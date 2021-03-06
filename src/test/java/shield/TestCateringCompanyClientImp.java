/**
 *
 */

package shield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */

public class TestCateringCompanyClientImp {
    private final static String clientPropsFilename = "client.cfg";
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = "EH8_5FL";
    String chi = String.valueOf(42);
    private Properties clientProps;
    private CateringCompanyClient client;
    private int testOrderNumber;

    private Properties loadProperties(String propsFilename) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();

        try {
            InputStream propsStream = loader.getResourceAsStream(propsFilename);
            props.load(propsStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return props;
    }

    @BeforeEach
    public void setup() {
        clientProps = loadProperties(clientPropsFilename);

        client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    }


    @Test
    public void testRegisterCateringCompany() {
        Random rand = new Random();
        String name = String.valueOf(rand.nextInt(10000));
        String postCode = "EH8_5FL";

        assertTrue(client.registerCateringCompany(name, postCode));
        //assertTrue(client.isRegistered());
        //assertEquals(client.getName(), name);
    }

    @Test
    public void testCateringCompanyUpdateOrderStatus() {
        try {
            String respond = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerShieldingIndividual?CHI=" + chi);
            System.out.println(respond);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            testOrderNumber = Integer.parseInt(ClientIO.doPOSTRequest(clientProps.getProperty("endpoint") + "/placeOrder?individual_id=" +
                            chi + "&catering_business_name=" + name + "&catering_postcode=" + postCode,
                    "{\"contents\": [{\"id\":1,\"name\":\"cucumbers\",\"quantity\":20},{\"id\":2,\"name\":\"tomatoes\",\"quantity\":2}]}"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(client.updateOrderStatus(testOrderNumber, "packed"));
        assertTrue(client.updateOrderStatus(testOrderNumber, "dispatched"));
        assertTrue(client.updateOrderStatus(testOrderNumber, "delivered"));
        assertTrue(!client.updateOrderStatus(testOrderNumber, "gibberish"));
    }
}
