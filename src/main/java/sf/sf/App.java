package sf.sf;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.google.common.base.Throwables;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import sf.sf.bin.DbReaderInvoker;
import sf.sf.bin.RequesterSaver;
import sf.sf.command.S3Command;
import sf.sf.s3client.S3Client;
import sf.sf.storage.DbLogic;
import static sf.sf.Constants.*;
/**
 * Main entrypoint into app.
 * 
 * @author ari
 *
 */
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);
    private final Vertx vertx;
	private CliOptions cliOptions = new CliOptions();

	public static void main(String[] args) throws IOException {
		//This is necessary to get netty debug logs using log4j2
		InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

		App app= new App(args);
		app.go();
	
	}
	
	
	public App(String[] args){
		vertx= Vertx.vertx();
		this.setupCliOptions(args);
		S3Client.setup(vertx, cliOptions);
	}
	
	public void go() throws IOException{
		List<String> commands = cliOptions.getAllCommands();
		logger.debug("------Full list of commands: "+commands.toString());
		DbLogic.setupDb(cliOptions, commands);
		this.executeCommands(commands);
	}
	
	/**
	 *  Parse args from properties file and override with command line args.
	 */
	private void setupCliOptions(String[] args){
		new JCommander(cliOptions, args);
		
		//Parse default properties file param. 
		MyPropertiesDefaultProvider defaultProvider =
				new MyPropertiesDefaultProvider(cliOptions.getPropsFile());
		
		 //Parse again, this time using default properties file
		JCommander jc = new JCommander();
		jc.setDefaultProvider(defaultProvider);
		jc.addObject(cliOptions);
		jc.parse(args);
		
		//JCommander sets the properties in cliOptions. No need to return anything.
	}
	
	/**
	 * Run commands.
	 * 
	 * @param commands
	 * @throws IOException
	 */
	private void executeCommands(List<String> commands)
			throws IOException{
		
		int commandCount=0;
		for(String command: commands){
			commandCount++;
			S3Command.numRequests = 0;
			
			boolean isLastCommand = this.isLastCommand(commandCount, commands.size());
			Future<String> commandDoneFuture = this.makeCommandDoneFuture(isLastCommand, command);	
			
			this.runCommand(commandDoneFuture, command);
			
			this.waitForCommandToComplete(commandDoneFuture);
			
		}
		
	}
	
	private void runCommand(Future<String> commandDoneFuture, String command){

		try{
			//Pass blocking code handler to the commanders so they can call complete
			if(REQUESTER_SAVER_COMMANDS.contains(command)){
				RequesterSaver commander =
						new RequesterSaver(this.vertx, cliOptions, command, commandDoneFuture);
				commander.run();	
			}else if(READER_COMMANDS.contains(command)){
				DbReaderInvoker commander = new DbReaderInvoker(cliOptions, command);
				commander.run();
				commandDoneFuture.complete();
			}

		}catch(IOException e){
			logger.error("Exception running command " +command, e);
		}
		
	}
	
	private void waitForCommandToComplete(Future<String> commandDoneFuture) {
		while(!commandDoneFuture.isComplete()){
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				logger.error(e)	;
			}
		}
	}
	
	private Future<String> makeCommandDoneFuture(boolean lastCommand, String command){
		
		Handler<AsyncResult<String>> commandResultHandler= this.makeEndHandler(lastCommand, command);
		Future<String> commandDoneFuture = Future.future();
		commandDoneFuture.setHandler(commandResultHandler);
		return commandDoneFuture;
	}
	
	private boolean isLastCommand(int commandCount, int commandsSize){
		boolean lastCommand =false;
		if(commandCount == commandsSize){
			lastCommand = true;
		}
		return lastCommand;
	}
	
	public Handler<AsyncResult<String>> makeEndHandler(boolean lastCommand, String command){
		Handler<AsyncResult<String>> handler = (AsyncResult<String> result) -> {
			DbLogic.logSizes();

			if(result.succeeded()){
				logger.debug(command+" ----Command succeeded " +result.result());
			}else{
				logger.error(command +"----Command failed "+result.result());
				String s = Throwables.getStackTraceAsString(result.cause());
				logger.error(s);
			}
			
			if(lastCommand){
				this.closeResources();
			}
			
		
		};
		
		return handler;
	}


	private void closeResources() {
		logger.debug("Commiting db");
		DbLogic.commitObjectsDb();

		logger.debug("Closing db");
		DbLogic.closeObjectsDb();
		logger.debug("Closing vertx");

		this.vertx.close((AsyncResult<Void> closeResult)->{

			if(!closeResult.succeeded()){
				String s = Throwables.getStackTraceAsString(closeResult.cause());
				logger.error("Error closing vertx {} ", closeResult.cause());
				logger.error(s);
			}else{
				logger.debug("vertx close succeeded");
			}
		});
	}
	
	
	
}
