package shield;

import org.junit.jupiter.api.*;

import java.util.*;
import java.time.LocalDateTime;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
    private final static String clientPropsFilename = "client.cfg";

    private Properties clientProps;
    private ShieldingIndividualClient client;

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

        client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    }

    /**
     * Including use case:
     * Register Shielding Individual
     */
    @Test
    public void testShieldingIndividualNewRegistration() {
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);
    }

    /**
     * Including use cases:
     * Register Shielding Individual
     * Place Order
     */
    @Test
    public void testPlaceOrder() {
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);
        client.showFoodBoxes("none");
        assertTrue(client.pickFoodBox(1));
        assertTrue(client.changeItemQuantityForPickedFoodBox(2, 1));
        client.getCateringCompanies();
        assertTrue(client.placeOrder());
        int id = client.getOrderNumbers().iterator().next();
        assertEquals(client.getStatusForOrder(id), "Placed");
        assertTrue(client.requestOrderStatus(id));
        assertEquals(client.getStatusForOrder(id), "Placed");

        //check contents of the order
        assertEquals(client.getItemIdsForOrder(id), Arrays.asList(1,2,6));

        assertEquals(client.getItemNameForOrder(1,id),"cucumbers");
        assertEquals(client.getItemQuantityForOrder(1,id),1);
        assertEquals(client.getItemNameForOrder(2,id),"tomatoes");
        assertEquals(client.getItemQuantityForOrder(2,id),1);
        assertEquals(client.getItemNameForOrder(6,id),"pork");
        assertEquals(client.getItemQuantityForOrder(6,id),1);

        assertEquals(client.getItemQuantityForOrder(9,id),0);
        assertNull(client.getItemNameForOrder(9, id));

    }

    /**
     * Including use cases:
     * Register Shielding Individual
     * Place Order
     * Edit Food Box Order
     */
    @Test
    public void testEditOrder() {
        //generate random valid CHI and register
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);

        client.showFoodBoxes("pollotarian");
        //using knowledge from food_boxes.txt
        assertEquals(client.getItemQuantityForFoodBox(1, 2), 2);
        assertEquals(client.getItemQuantityForFoodBox(3,3),1);

        assertTrue(client.pickFoodBox(2));
        assertTrue(client.changeItemQuantityForPickedFoodBox(1, 1));
        client.getCateringCompanies();
        assertTrue(client.placeOrder());
        int id = client.getOrderNumbers().iterator().next();
        assertEquals(client.getStatusForOrder(id), "Placed");

        //setItemQuantityForOrder and then editOrder
        assertTrue(client.setItemQuantityForOrder(1, id, 1));
        assertTrue(client.editOrder(id));
        assertEquals(client.getItemQuantityForOrder(1, id), 1);
        assertTrue(client.setItemQuantityForOrder(1, id, 0));
        assertTrue(client.editOrder(id));
        //new quantity should be smaller than current quantity in the server.
        assertFalse(client.setItemQuantityForOrder(1, id, 1));
    }

    /**
     * Including use cases:
     * Register Shielding Individual
     * Place Order
     * Cancel Order
     * Request status
     */
    @Test
    public void testCancelOrder() {
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);
        client.showFoodBoxes("pollotarian");
        assertTrue(client.pickFoodBox(2));
        assertTrue(client.changeItemQuantityForPickedFoodBox(1, 1));
        client.getCateringCompanies();
        assertTrue(client.placeOrder());
        int id = client.getOrderNumbers().iterator().next();
        assertEquals(client.getStatusForOrder(id), "Placed");
        assertTrue(client.cancelOrder(id));
        assertTrue(client.requestOrderStatus(id));
        assertEquals(client.getStatusForOrder(id), "Cancelled");
    }

    /**
     * It should be unable to place an order
     * immediately after an order is placed
     * at least 7 days interval needed
     */
    @Test
    public void testPlaceOrderAfterPlaceOrder() {
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        client.showFoodBoxes("pollotarian");
        assertTrue(client.pickFoodBox(2));
        client.getCateringCompanies();
        assertTrue(client.placeOrder());
        //set current food box again
        client.showFoodBoxes("pollotarian");
        assertTrue(client.pickFoodBox(3));
        //unable to place!
        assertFalse(client.placeOrder());
    }

    /**
     * showFoodBoxes() should return empty list
     * if input is invalid
     */
    @Test
    public void testShowFoodBoxesInvalid() {
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);

        assertEquals(client.showFoodBoxes("invalidInput"), Collections.emptyList());
    }

    @Test
    public void testShowFoodBoxes() {
        //generate random valid CHI and register
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000 + random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);

        client.showFoodBoxes("pollotarian");
        //using knowledge from food_boxes.txt
        assertEquals(client.getItemNameForFoodBox(1,2),"cucumbers");
        assertEquals(client.getItemQuantityForFoodBox(1, 2), 2);
        assertEquals(client.getItemNameForFoodBox(3,3),"onions");
        assertEquals(client.getItemQuantityForFoodBox(3,3),1);

        assertEquals(client.getItemsNumberForFoodBox(1),4);
        assertEquals(client.getItemsNumberForFoodBox(4),4);
        assertEquals(client.getDietaryPreferenceForFoodBox(3),"none");
        assertEquals(client.getDietaryPreferenceForFoodBox(5),"vegan");
        assertEquals(client.getFoodBoxNumber(),5);
        assertEquals(client.getItemIdsForFoodBox(2),Arrays.asList(1,3,7));
        assertEquals(client.getItemIdsForFoodBox(3),Arrays.asList(3,4,8));

    }
}
