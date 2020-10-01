package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.target.Target;
import au.com.dius.pact.provider.junitsupport.target.TestTarget;

import org.junit.runner.RunWith;

@Provider("Idam_api")
@PactBroker(host = "localhost", port = "9292")
@RunWith(PactRunner.class)
public class PactProviderTest {
    //this fetches the stated provider pacts from the broker and runs them
    @TestTarget
    public final Target target = new HttpTarget();

    @State("a list of roles are available in role assignment service")
    public void listOfRoles() {
        //empty
    }
}
