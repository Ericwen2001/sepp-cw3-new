/**
 *
 */

package shield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */

public class SupermarketClientImpTest {
    private final static String clientPropsFilename = "client.cfg";

    private Properties clientProps;
    private SupermarketClient client;
    private final Random rand = new Random();
    private final int testOrderNumber = rand.nextInt(10000);
    private final String name = String.valueOf(rand.nextInt(10000));
    private final String postCode = "EH8_5FL";
    private final int random = rand.nextInt(100000);
    private final String chi = String.valueOf(1210000000 + random);

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

        client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    }


    @Test
    public void testSupermarketNewRegistration() {
        Random rand = new Random();
        String name = String.valueOf(rand.nextInt(10000));
        String postCode = String.valueOf(rand.nextInt(10000));

        assertTrue(client.registerSupermarket(name, postCode));
        assertTrue(client.isRegistered());
        assertEquals(client.getName(), name);
    }

    @Test
    public void testRecordSupermarketOrder() {
        ShieldingIndividualClientImp individual = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        individual.registerShieldingIndividual(chi);
        individual.getCateringCompanies();
        client.registerSupermarket(name, postCode);

        assertTrue(client.recordSupermarketOrder(chi, testOrderNumber));
    }

    @Test
    public void testSupermarketUpdateOrderStatus() {
        ShieldingIndividualClientImp individual = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        individual.registerShieldingIndividual(chi);
        individual.getCateringCompanies();
        client.registerSupermarket(name, postCode);
        client.recordSupermarketOrder(chi, testOrderNumber);

        assertTrue(client.updateOrderStatus(testOrderNumber, "packed"));
        assertTrue(client.updateOrderStatus(testOrderNumber, "dispatched"));
        assertTrue(client.updateOrderStatus(testOrderNumber, "delivered"));
        assertTrue(!client.updateOrderStatus(testOrderNumber, "gibberish"));
    }
}
