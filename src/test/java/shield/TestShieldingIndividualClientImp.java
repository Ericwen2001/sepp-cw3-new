package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;

public class TestShieldingIndividualClientImp {
    private final String clientPropsFilename = "client.cfg";
    private String endpoint;
    private Properties clientProps;
    private ShieldingIndividualClientImp clientImp;

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
    void setup() {
        clientProps = loadProperties(clientPropsFilename);
        endpoint = clientProps.getProperty("endpoint");
        clientImp = new ShieldingIndividualClientImp(endpoint);
    }

    @Test
    public void testRegisterShieldingIndividual() {
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int day = rand.nextInt(28) + 1;
            int month = rand.nextInt(12) + 1;
            int year = rand.nextInt(100);
            int tail = rand.nextInt(10000);
            String Day = String.format("%02d", day);
            String Month = String.format("%02d", month);
            String Year = String.format("%02d", year);
            String Tail = String.format("%04d", tail);
            String chi = Year + Month + Day + Tail;
            assertTrue(clientImp.registerShieldingIndividual(chi));
        }
        // test 2.29
        assertTrue(clientImp.registerShieldingIndividual("0402291234"));
        assertFalse(clientImp.registerShieldingIndividual("0002291234"));

        assertFalse(clientImp.registerShieldingIndividual("0109311234"));
        assertTrue(clientImp.registerShieldingIndividual("4801311234"));
    }

    @Test
    public void testShowFoodBoxes() {
        Collection<String> expectedNone = Arrays.asList("1", "3", "4");
        Collection<String> expectedPool = Collections.singletonList("2");
        Collection<String> expectedVe = Collections.singletonList("5");
        assertEquals(clientImp.showFoodBoxes("none"), expectedNone);
        assertEquals(clientImp.showFoodBoxes("pollotarian"), expectedPool);
        assertEquals(clientImp.showFoodBoxes("vegan"), expectedVe);
    }

    /**
     * pickFoodBox() set currentFoodBox according to availableFoodBoxFromServer
     * this test set availableFoodBoxFromServer and check currentFoodBox
     */
    @Test
    public void testPickFoodBox() {
        //should return false as availableFoodBoxFromServer is null
        assertFalse(clientImp.pickFoodBox(1));

        //set availableFoodBoxFromServer
        ShieldingIndividualClientImp.Content apple =
                new ShieldingIndividualClientImp.Content(1, "apple", 2);
        ShieldingIndividualClientImp.Content orange =
                new ShieldingIndividualClientImp.Content(2, "orange", 2);
        ShieldingIndividualClientImp.Content pork =
                new ShieldingIndividualClientImp.Content(3, "pork", 1);
        List<ShieldingIndividualClientImp.Content> contentA = Arrays.asList(apple, orange, pork);
        List<ShieldingIndividualClientImp.Content> contentB = Arrays.asList(apple, orange);
        ShieldingIndividualClientImp.FoodBox a =
                new ShieldingIndividualClientImp.FoodBox(contentA, "abc", "none", "1", "box a");
        ShieldingIndividualClientImp.FoodBox b =
                new ShieldingIndividualClientImp.FoodBox(contentB, "abc", "vegan", "2", "box b");
        List<ShieldingIndividualClientImp.FoodBox> foodBoxList = Arrays.asList(a, b);
        clientImp.setAvailableFoodBoxFromServer(foodBoxList);

        //check currentFoodBox
        assertTrue(clientImp.pickFoodBox(1));
        assertEquals(clientImp.getCurrentFoodBox().getId(), "1");
        assertEquals(clientImp.getCurrentFoodBox().getName(), "box a");
        assertEquals(clientImp.getCurrentFoodBox().getContents(), contentA);
        assertTrue(clientImp.pickFoodBox(2));
        assertEquals(clientImp.getCurrentFoodBox().getId(), "2");
        assertEquals(clientImp.getCurrentFoodBox().getName(), "box b");
        assertEquals(clientImp.getCurrentFoodBox().getContents(), contentB);
    }

    @Test
    public void testGetItemIdsForFoodBox() {

        Collection<Integer> expectO = Arrays.asList(1, 2, 6);
        Collection<Integer> expectTwo = Arrays.asList(1, 3, 7);
        Collection<Integer> expectThree = Arrays.asList(3, 4, 8);
        Collection<Integer> expectF = Arrays.asList(13, 11, 8, 9);
        Collection<Integer> expectFive = Arrays.asList(9, 11, 12);
        assertEquals(clientImp.getItemIdsForFoodBox(1), expectO);
        assertEquals(clientImp.getItemIdsForFoodBox(2), expectTwo);
        assertEquals(clientImp.getItemIdsForFoodBox(3), expectThree);
        assertEquals(clientImp.getItemIdsForFoodBox(4), expectF);
        assertEquals(clientImp.getItemIdsForFoodBox(5), expectFive);
    }

    @Test
    public void testGetItemQuantityForFoodBox() {
        setAvailableFoodBoxFromServer();

        //test getItemQuantityForFoodBox using knowledge from food_boxes.txt
        assertEquals(clientImp.getItemQuantityForFoodBox(1, 1), 1);
        assertEquals(clientImp.getItemQuantityForFoodBox(1, 2), 2);
        assertEquals(clientImp.getItemQuantityForFoodBox(4, 3), 2);
        assertEquals(clientImp.getItemQuantityForFoodBox(9, 5), 1);
        assertEquals(clientImp.getItemQuantityForFoodBox(9, 2), 0);
    }

    private void setAvailableFoodBoxFromServer() {
        String requestAllPreference = "/showFoodBox?orderOption=catering&dietaryPreference=";
        try {
            String responseAll = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + requestAllPreference);
            Type listType = new TypeToken<List<ShieldingIndividualClientImp.FoodBox>>() {
            }.getType();
            List<ShieldingIndividualClientImp.FoodBox> responseBoxes  = new Gson().fromJson(responseAll, listType);
            clientImp.setAvailableFoodBoxFromServer(responseBoxes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * changeItemQuantityForPickedFoodBox() change the item quantity in
     * currentFoodBox, this test set availableFoodBoxFromServer and thus set
     * currentFoodBox using "box a" in food_boxes.txt
     */
    @Test
    public void testChangeItemQuantityForPickedFoodBox() {
        assertFalse(clientImp.changeItemQuantityForPickedFoodBox(4, 1));
        setAvailableFoodBoxFromServer();
        clientImp.setCurrentFoodBox(clientImp.getAvailableFoodBoxFromServer().get(0));

        List<ShieldingIndividualClientImp.Content> cs;

        //invalid quantity
        assertFalse(clientImp.changeItemQuantityForPickedFoodBox(2, -10));
        //invalid itemId
        assertFalse(clientImp.changeItemQuantityForPickedFoodBox(9, 1));
        //test valid case
        assertTrue(clientImp.changeItemQuantityForPickedFoodBox(2, 0));
        cs = clientImp.getCurrentFoodBox().getContents();
        for (ShieldingIndividualClientImp.Content c : cs) {
            if (c.getId() == 2) {
                assertEquals(c.getQuantity(), 0);
            }
        }
        assertTrue(clientImp.changeItemQuantityForPickedFoodBox(1, 5));
        for (ShieldingIndividualClientImp.Content c : cs) {
            if (c.getId() == 1) {
                assertEquals(c.getQuantity(), 5);
            }
        }
    }

    /**
     * getCateringCompanies() set availableCatererFromServer
     * and return their names;
     * each Caterer contain name, postCode,
     * and the distance between this caterer and individual
     * <p>
     * in this test, Individual "tester" and Caterer "testCatering1"
     * has same postcode,
     * so the distance between them should be 0
     */
    @Test
    public void testGetCateringCompanies() {
        clientImp.setIndividual(new ShieldingIndividualClientImp.Individual
                ("tester", "tester", "EH7 5FL", "12345"));
        try {
            ClientIO.doGETRequest(clientProps.getProperty("endpoint") +
                    "/registerCateringCompany?business_name=testCatering1&postcode=EH7_5FL");
            ClientIO.doGETRequest(clientProps.getProperty("endpoint") +
                    "/registerCateringCompany?business_name=testCatering2&postcode=EH9_5FL");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collection<String> companiesName = clientImp.getCateringCompanies();
        assertTrue(companiesName.contains("testCatering1") &&
                companiesName.contains("testCatering2"));
        List<ShieldingIndividualClientImp.Caterer> caterers = clientImp.getAvailableCatererFromServer();
        for (ShieldingIndividualClientImp.Caterer c : caterers) {
            if (c.name.equals("testCatering1")) {
                assertEquals(c.distance, 0);
            }
        }
    }

    @Test
    public void testGetClosestCateringCompany() {
        ShieldingIndividualClientImp.Caterer c1, c2;
        c1 = new ShieldingIndividualClientImp.Caterer("1", "c1", "postcode1", 0);
        c2 = new ShieldingIndividualClientImp.Caterer("1", "c2", "postcode2", 100);
        clientImp.setAvailableCatererFromServer(Arrays.asList(c1, c2));

        assertEquals(clientImp.getClosestCateringCompany(), "c1");
    }
    @Test
    public void testPlaceOrder() {
        clientImp.setChi("0110121234");
        //register individual in server
        try {
            String request = "/registerShieldingIndividual?CHI=0110121234";
            ClientIO.doGETRequest(endpoint+request);
        }catch (Exception e) {
            e.printStackTrace();
        }

        setAvailableFoodBoxFromServer();
        clientImp.setCurrentFoodBox(clientImp.getAvailableFoodBoxFromServer().get(0));
        clientImp.setIndividual(new ShieldingIndividualClientImp.Individual("Eric","K","EH7 5FL","123123"));
        ShieldingIndividualClientImp.Caterer c1, c2;
        c1 = new ShieldingIndividualClientImp.Caterer("1", "c1", "EH7_5FL", 0);
        c2 = new ShieldingIndividualClientImp.Caterer("2", "c2", "EH9_5FL", 100);
        clientImp.setAvailableCatererFromServer(Arrays.asList(c1, c2));

        //register c1 and c2
        try {
            String request1 = "/registerCateringCompany?business_name=c1&postcode=EH7_5FL";
            String request2 = "/registerCateringCompany?business_name=c2&postcode=EH9_5FL";
            ClientIO.doGETRequest(endpoint+request1);
            ClientIO.doGETRequest(endpoint+request2);
        }catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(clientImp.placeOrder());
        String orderID = clientImp.getOrders().keySet().iterator().next();
        //check order status from the server
        String request = "/requestStatus?order_id=" + orderID;
        String response = null;
        try {
            response = ClientIO.doGETRequest(endpoint+request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(response,"0");

    }


}
