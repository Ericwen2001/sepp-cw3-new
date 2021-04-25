/**
 *
 */

package shield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */

public class CateringCompanyClientImpTest {
    private final static String clientPropsFilename = "client.cfg";

    private Properties clientProps;
    private CateringCompanyClient client;
    private int testOrderNumber;
    private final Random rand = new Random();
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

        client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    }


    @Test
    public void testCateringCompanyNewRegistration() {
        Random rand = new Random();
        String name = String.valueOf(rand.nextInt(10000));
        String postCode = "EH8_5FL";

        assertTrue(client.registerCateringCompany(name, postCode));
        assertTrue(client.isRegistered());
        assertEquals(client.getName(), name);
    }

    @Test
    public void testCateringCompanyUpdateOrderStatus() {
        ShieldingIndividualClientImp individual = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        individual.registerShieldingIndividual(chi);
        individual.getCateringCompanies();
        Collection<String> foodBoxes = individual.showFoodBoxes("none");
        individual.pickFoodBox(Integer.parseInt(foodBoxes.iterator().next()));
        individual.placeOrder();
        testOrderNumber = individual.getOrderNumbers().iterator().next();

        assertTrue(client.updateOrderStatus(testOrderNumber, "packed"));
        assertTrue(client.updateOrderStatus(testOrderNumber, "dispatched"));
        assertTrue(client.updateOrderStatus(testOrderNumber, "delivered"));
        assertTrue(!client.updateOrderStatus(testOrderNumber, "gibberish"));
    }
}
