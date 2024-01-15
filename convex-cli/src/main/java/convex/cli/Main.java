package convex.cli;

import java.io.Console;
import java.io.File;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import convex.api.Convex;
import convex.cli.client.Query;
import convex.cli.client.Status;
import convex.cli.client.Transaction;
import convex.cli.key.Key;
import convex.cli.local.Local;
import convex.cli.output.RecordOutput;
import convex.cli.peer.Peer;
import convex.core.Result;
import convex.core.crypto.AKeyPair;
import convex.core.crypto.PFXTools;
import convex.core.exceptions.TODOException;
import convex.core.util.Utils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;

/**
* Convex CLI implementation
*/
@Command(name="convex",
	subcommands = {
		Account.class,
		Key.class,
		Local.class,
		Peer.class,
		Query.class,
		Status.class,
		Transaction.class,
		CommandLine.HelpCommand.class
	},
	usageHelpAutoWidth=true,
	sortOptions = true,
	mixinStandardHelpOptions=true,
	// headerHeading = "Usage:",
	// synopsisHeading = "%n",
	parameterListHeading = "%nParameters:%n",
	optionListHeading = "%nOptions:%n",
	commandListHeading = "%nCommands:%n",
	versionProvider = Main.VersionProvider.class,
	description="Convex Command Line Interface")

public class Main implements Runnable {

	private static Logger log = LoggerFactory.getLogger(Main.class);

	public CommandLine commandLine=new CommandLine(this);

	@Option(names={ "-c", "--config"},
		scope = ScopeType.INHERIT,
		description="Use the specified config file. If not specified, will check ~/.convex/convex.config")
	private String configFilename;

    @Option(names={"-e", "--etch"},
		scope = ScopeType.INHERIT,
		description="Convex Etch database filename. A temporary storage file will be created if required.")
	private String etchStoreFilename;

	@Option(names={"-k", "--keystore"},
		defaultValue="${env:CONVEX_KEYSTORE_PASSWORD:-" +Constants.KEYSTORE_FILENAME+"}",
		scope = ScopeType.INHERIT,
		description="Keystore filename. Default: ${DEFAULT-VALUE}")
	private String keyStoreFilename;

	@Option(names={"-p", "--password"},
		scope = ScopeType.INHERIT,
		//defaultValue="",
		description="Password to read/write to the Keystore")
	private String password;
	
	@Option(names={"-n", "--noninteractive"},
			scope = ScopeType.INHERIT,
			//defaultValue="",
			description="Specify to disable interactive prompts")
	private boolean nonInteractive;

    @Option(names={ "-v", "--verbose"},
		scope = ScopeType.INHERIT,
		defaultValue="${env:CONVEX_VERBOSE_LEVEL:-2}",
		description="Show more verbose log information. You can increase verbosity by using multiple -v or -vvv")
	private Integer verbose;

	public Main() {
		commandLine=commandLine.setExecutionExceptionHandler(new Main.ExceptionHandler());
	}

	@Override
	public void run() {
		// no command provided - so show help
		CommandLine.usage(new Main(), System.out);
	}

	/**
	 * Java main(...) entry point when run as a Java application.
	 * Exits JVM process with specified exit code
	 * @param args Command line arguments 
	 */
	public static void main(String[] args) {
		Main mainApp = new Main();
		int result = mainApp.mainExecute(args);
		System.exit(result);
	}

	/**
	 * Command line execution entry point. Can be run from Java code without 
	 * terminating the JVM.
	 * @param args Command line arguments
	 * @return Process result value
	 */
	public int mainExecute(String[] args) {
		commandLine
		.setUsageHelpLongOptionsMaxWidth(40)
		.setUsageHelpWidth(40 * 4);

		// do  a pre-parse to get the config filename. We need to load
		// in the defaults before running the full execute
		try {
			commandLine.parseArgs(args);
		} catch (Throwable t) {
			log.debug("Unable to parse arguments: " + t);
		}

		ch.qos.logback.classic.Logger parentLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

		Level[] verboseLevels = {Level.OFF,Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL};
		
		if (verbose==null) verbose=0;
		if (verbose >= 0 && verbose <= verboseLevels.length) {
			parentLogger.setLevel(verboseLevels[verbose]);
		} else {
			throw new CLIError("Invalid verbosoity level: "+verbose);
		}
		
		int result = 0;
		result = commandLine.execute(args);
		return result;
	}
	
	/**
	 * Version provider class
	 */
	public static final class VersionProvider implements IVersionProvider {
		@Override
		public String[] getVersion() throws Exception {
			String s=Main.class.getPackage().getImplementationVersion();
			return new String[] {s};
		}

	}
	
	/**
	 * Exception handler class
	 */
	private class ExceptionHandler implements IExecutionExceptionHandler {

		@Override
		public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult)
				throws Exception {
			PrintWriter err = commandLine.getErr();
			if (ex instanceof CLIError) {
				CLIError ce=(CLIError)ex;
				err.println(ce.getMessage());
				Throwable cause=ce.getCause();
				if (cause!=null) {
					err.println("Underlying cause: ");
					cause.printStackTrace(err);
				}
			} else {
				ex.printStackTrace(err);
			}
			// Exit with correct code for exception type
			return ExitCodes.getExitCode(ex);
		}

	}
	
	/**
	 * Get the currently configured password for the keystore. Will emit warning and default to
	 * blank password if not provided
	 * @return Password string
	 */
	public char[] getStorePassword() {
		char[] storepass=null;
		
		if(this.password!=null) {
			storepass=this.password.toCharArray();	
		} else {	
			if (!nonInteractive) {
				Console console = System.console(); 
				storepass= console.readPassword("Keystore Password: ");
			} 
			
			if (storepass==null) {
				log.warn("No password for keystore: defaulting to blank password");
				storepass=new char[0];
			}
		}
		return storepass;
	}
	
	/**
	 * Keys the password for the current key 
	 * @return
	 */
	public char[] getKeyPassword() {
		char[] keypass=null;

		if(this.password!=null) {
			keypass=this.password.toCharArray();	
		} else {
			if (!nonInteractive) {
				Console console = System.console(); 
				keypass= console.readPassword("Keystore Password: ");
			} 
			
			if (keypass==null) {
				log.warn("No password for keystore: defaulting to blank password");
				keypass=new char[0];
			}

		}
		return keypass;
	}
	
	/**
	 * Sets the currently defined keystore password
	 * @param password Password to use
	 */
	public void setPassword(String password) {
		this.password=password;
	}

	/**
	 * Gets the keystore file name currently used for the CLI
	 * @return File name, or null if not specified
	 */
	public String getKeyStoreFilename() {
		if ( keyStoreFilename != null) {
			return Helpers.expandTilde(keyStoreFilename).strip();
		}
		return null;
	}

	public String getEtchStoreFilename() {
		if ( etchStoreFilename != null) {
			return Helpers.expandTilde(etchStoreFilename).strip();
		}
		return null;
	}

	private boolean keyStoreLoaded=false;
	private KeyStore keyStore=null;
	
	/**
	 * Gets the current key store
	 * @return KeyStore instance, or null if it does not exist
	 */
	public KeyStore getKeystore() {
		if (keyStoreLoaded==false) {
			keyStore=loadKeyStore(false);
			keyStoreLoaded=true;
		}
		return keyStore;
	}
	
	/**
	 * Gets the current key store. 
	 * @param create Flag to indicate if keystore should be created
	 * @return KeyStore instance
	 */
	public KeyStore getKeystore(boolean create) {
		if (keyStoreLoaded==false) {
			keyStore=loadKeyStore(create);
			if (keyStore==null) throw new CLIError("Keystore does not exist!");
			keyStoreLoaded=true;
		}
		return keyStore;
	}
	
	/**
	 * Loads the currently configured get Store
	 * @param isCreate Flag to indicate if keystore should be created if absent
	 * @return KeyStore instance, or null if does not exist
	 */
	public KeyStore loadKeyStore(boolean isCreate)  {
		char[] password=getStorePassword();
		File keyFile = new File(getKeyStoreFilename());
		try {
			if (keyFile.exists()) {
				keyStore = PFXTools.loadStore(keyFile, password);
			} else if (isCreate) {
				log.debug("No keystore exists, creating at: "+keyFile.getCanonicalPath());
				Helpers.createPath(keyFile);
				keyStore = PFXTools.createStore(keyFile, password);
			}
		} catch (Exception t) {
			throw new CLIError("Unable to read keystore at: "+keyFile,t);
		}
		keyStoreLoaded=true;
		return keyStore;
	}

	/**
	 * Loads a keypair from configured keystore
	 * @param publicKey String identifying the public key. May be a prefix
	 * @return Keypair instance, or null if not found
	 */
	public AKeyPair loadKeyFromStore(String publicKey) {
		if (publicKey==null) return null;
		
		AKeyPair keyPair = null;

		publicKey = publicKey.trim();
		publicKey = publicKey.toLowerCase().replaceFirst("^0x", "").strip();
		if (publicKey.isEmpty()) {
			return null;
		}
		
		char[] storePassword=getStorePassword();

		File keyFile = new File(getKeyStoreFilename());
		try {
			if (!keyFile.exists()) {
				throw new Error("Cannot find keystore file "+keyFile.getCanonicalPath());
			}
			KeyStore keyStore = PFXTools.loadStore(keyFile, storePassword);

			Enumeration<String> aliases = keyStore.aliases();

			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				if (alias.indexOf(publicKey) == 0) {
					log.trace("found keypair " + alias);
					keyPair = PFXTools.getKeyPair(keyStore, alias, storePassword);
					break;
				}
			}
		} catch (Throwable t) {
			throw new CLIError("Cannot load key store",t);
		}

		return keyPair;
	}

	/**
	 * Generate key pairs and add to store. Does not save store!
	 * @param count Number of key pairs to generate
	 * @return List of key pairs
	 */
	public List<AKeyPair> generateKeyPairs(int count, char[] keyPassword) {
		List<AKeyPair> keyPairList = new ArrayList<>(count);

		// generate `count` keys
		for (int index = 0; index < count; index ++) {
			AKeyPair keyPair = AKeyPair.generate();
			keyPairList.add(keyPair);
			addKeyPairToStore(keyPair,keyPassword);
		}

		return keyPairList;
	}

	/**
	 * Adds key pair to store. Does not save keystore!
	 * @param keyPair Keypair to add
	 */
	public void addKeyPairToStore(AKeyPair keyPair, char[] keyPassword) {

		KeyStore keyStore = getKeystore();
		if (keyStore==null) {
			throw new CLIError("Trying to add key pair but keystore does not exist");
		}
		try {
			// save the key in the keystore
			PFXTools.setKeyPair(keyStore, keyPair, keyPassword);
		} catch (Throwable t) {
			throw new CLIError("Cannot store the key to the key store "+t);
		}

	}
	
	/**
	 * Connect as a client to the currently configured Convex network
	 * @return Convex instance
	 */
	public Convex connect() {
		throw new TODOException();
	}
	
	public void saveKeyStore() {
		// save the keystore file
		if (keyStore==null) throw new CLIError("Trying to save a keystore that has not been loaded!");
		try {
			PFXTools.saveStore(keyStore, new File(getKeyStoreFilename()), getStorePassword());
		} catch (Throwable t) {
			throw Utils.sneakyThrow(t);
		}
	}



	public void println(String s) {
		if (s==null) s="null";
		commandLine.getOut().println(s);
	}

	public void printError(Result result) {
		commandLine.getErr().println(result.toString());
	}
	
	public void printResult(Result result) {
		commandLine.getOut().println(result.toString());
	}

	public void printRecord(RecordOutput output) {
		output.writeToStream(commandLine.getOut());
	}

	public void println(Object value) {
		println(Utils.toString(value));
	}


}
