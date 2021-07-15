package convex.cli;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import convex.api.Convex;
import convex.core.data.ACell;
import convex.core.lang.Reader;
import convex.core.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 *
 * Convex Query sub command
 *
 * 		convex.query
 *
 */
@Command(name="query",
	aliases={"qu"},
	mixinStandardHelpOptions=true,
	description="Execute a query on the current peer.")
public class Query implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Query.class);

	@ParentCommand
	protected Main mainParent;

	@Option(names={"--port"},
		description="Port number to connect to a peer.")
	private int port = 0;

	@Option(names={"--host"},
		defaultValue=Constants.HOSTNAME_PEER,
		description="Hostname to connect to a peer. Default: ${DEFAULT-VALUE}")
	private String hostname;

	@Parameters(paramLabel="queryCommand", description="Query Command")
	private String queryCommand;

	@Override
	public void run() {
		// sub command run with no command provided
		log.info("query command: {}", queryCommand);

		Convex convex = null;

		try {
			convex = mainParent.connectToSessionPeer(hostname, port, Main.initConfig.getUserAddress(0), null);
		} catch (Error e) {
			log.error(e.getMessage());
			return;
		}
		try {
			System.out.printf("Executing query: %s\n", queryCommand);
			ACell message = Reader.read(queryCommand);
			Result result = convex.querySync(message, 5000);
			System.out.println(result);
		} catch (IOException e) {
			log.error("Query Error: {}", e.getMessage());
			// TODO Auto-generated catch block
			// e.printStackTrace();
			return;
		}  catch (TimeoutException e) {
			log.error("Query timeout");
		}
	}

}
