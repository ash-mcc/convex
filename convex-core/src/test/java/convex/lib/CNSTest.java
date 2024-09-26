package convex.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import convex.core.data.AVector;
import convex.core.data.Address;
import convex.core.init.Init;
import convex.core.lang.ACVMTest;
import convex.core.lang.Context;
import convex.core.lang.TestState;

import static convex.test.Assertions.*;

public class CNSTest extends ACVMTest {
	
	Address REG=Init.REGISTRY_ADDRESS;
	
	@Override protected Context buildContext(Context ctx) {
		ctx=TestState.CONTEXT.fork();
		
		ctx=step(ctx,"(import convex.asset :as asset)");
		ctx=step(ctx,"(import convex.trust :as trust)");
		ctx=step(ctx,"(def cns #9)");
		return ctx;
	}
	
	@Test public void testConstantSetup() {
		assertEquals(Init.REGISTRY_ADDRESS,eval("*registry*"));
		assertEquals(Init.REGISTRY_ADDRESS,eval("cns"));
		assertEquals(Init.REGISTRY_ADDRESS,eval("@convex.registry"));
		
		// TODO: fix this
		// assertEquals(Init.REGISTRY_ADDRESS,eval("@cns"));
	}
	
	@Test public void testSpecial() {
		Context ctx=context();
		assertEquals(REG,eval(ctx,"*registry*"));
		// assertEquals(REG,eval(ctx,"cns"));
	}
	
	@Test public void testTrust() {
		// Root CNS node should only trust governance account
		assertFalse(evalB("(trust/trusted? [cns []] *address*)"));
		assertTrue(evalB("(query-as #6 `(~trust/trusted? [~cns []] *address*))"));
	}
	
	@Test public void testInit() {
		Address init=eval("(*registry*/resolve 'init)");
		assertEquals(Init.INIT_ADDRESS,init);
		
		assertEquals(eval("[#1 #6 nil nil]"), eval("(*registry*/read 'init)"));
	}
	
	@Test public void testCreateNestedFromTop() {
		Context ctx=context().forkWithAddress(Init.GOVERNANCE_ADDRESS);
		ctx=(step(ctx,"(*registry*/create 'foo.bar.bax #17)"));
		assertNotError(ctx);
		
		assertEquals(Address.create(17),eval(ctx,"(*registry*/resolve 'foo.bar.bax)"));
		assertNull(eval(ctx,"(*registry*/resolve 'foo.null.boo)"));
	}
	
	@Test public void testCreateTopLevel() {
		// HERO shouldn't be able to create a top level CNS entry
		assertTrustError(step("(*registry*/create 'foo)"));
		
		// NEed governance address to be able to create a top level CNS entry
		Context ctx=context().forkWithAddress(Init.GOVERNANCE_ADDRESS);
		ctx=step(ctx,"(import convex.trust :as trust)");
		ctx=(step(ctx,"(*registry*/create 'foo #17)"));
		assertNotError(ctx);
		ctx=step(ctx,"(def ref [*registry* [\"foo\"]])");
		AVector<?> ref=ctx.getResult();
		assertNotNull(ref);
		
		// System.out.println(eval(ictx,"*registry*/cns-database"));
		
		assertEquals(Address.create(17),eval(ctx,"(*registry*/resolve 'foo)"));
		
		ctx=(step(ctx,"(*registry*/create 'foo #666)"));
		assertEquals(Address.create(666),eval(ctx,"(*registry*/resolve 'foo)"));

		// HERO still shouldn't be able to update a top level CNS entry
		ctx=ctx.forkWithAddress(HERO);
		assertTrustError(step(ctx,"(*registry*/create 'foo *address* *address* {})"));
		assertTrustError(step(ctx,"(trust/change-control "+ref+" *address*)"));

	}

}
