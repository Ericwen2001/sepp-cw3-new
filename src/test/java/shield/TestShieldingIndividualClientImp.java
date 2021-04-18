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

    /**
     * generate some valid date and thus generate
     * valid Chi to test registerShieldingIndividual
     */
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

    /**
     * using setAvailableFoodBoxFromServer() to set
     * availableFoodBoxFromServer()
     * and thus test getItemNameForFoodBox()
     *
     */
    @Test
    public void testGetItemNameForFoodBox() {
        //test when availableFoodBoxFromServer == null
        assertNull(clientImp.getItemNameForFoodBox(0,0));

        //set availableFoodBoxFromServer
        setAvailableFoodBoxFromServer();

        assertEquals("cucumbers",clientImp.getItemNameForFoodBox(1,1));
        assertEquals("carrots",clientImp.getItemNameForFoodBox(4,3));

        //test when itemId 5 is not is foodBox 1
        assertNull(clientImp.getItemNameForFoodBox(5,1));

        //test when there is not foodBox with this foodBoxId
        assertNull(clientImp.getItemNameForFoodBox(5,99));
    }


    /**
     * using knowledge from food_boxes.txt to
     * test showFoodBoxes()
     */
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

    /**
     * set up "orders" using setUpOrdersAppleOrangePork()
     * hence test getItemIdsForOrder
     */
    @Test
    public void testGetItemIdsForOrder() {
        //when orderNumber is not valid
        assertNull(clientImp.getItemIdsForOrder(123));

        setUpOrdersAppleOrangePork();
        Collection<Integer> itemIds = Arrays.asList(1,2,3);
        assertEquals(itemIds,clientImp.getItemIdsForOrder(123));
    }

    /**
     * test getOrderNumbers when
     * orders is empty and when there is
     * one order
     */
    @Test
    public void testGetOrderNumbers() {
        Collection<Integer> emptyCollection = Collections.emptyList();
        assertEquals(clientImp.getOrderNumbers(),emptyCollection);
        setUpOrdersAppleOrangePork();
        Collection<Integer> orderId = Collections.singletonList(123);
        assertEquals(orderId,clientImp.getOrderNumbers());
    }

    /**
     * set up Orders with different orderStatus
     * and test getStatusForOrder()
     */
    @Test
    public void testGetStatusForOrder() {
        //test invalid orderNumber, should return null;
        assertNull(clientImp.getStatusForOrder(999));
        setUpOrdersAppleOrangePork();
        assertEquals("Placed",clientImp.getStatusForOrder(123));
        HashMap<String, ShieldingIndividualClientImp.Order> orders = new HashMap<>();
        ShieldingIndividualClientImp.Order o1 = new ShieldingIndividualClientImp.Order(null,1,0);
        ShieldingIndividualClientImp.Order o2 = new ShieldingIndividualClientImp.Order(null,2,0);
        ShieldingIndividualClientImp.Order o3 = new ShieldingIndividualClientImp.Order(null,3,0);
        ShieldingIndividualClientImp.Order o4 = new ShieldingIndividualClientImp.Order(null,4,0);
        orders.put("1",o1);
        orders.put("2",o2);
        orders.put("3",o3);
        orders.put("4",o4);
        clientImp.setOrders(orders);
        assertEquals("Packed",clientImp.getStatusForOrder(1));
        assertEquals("Dispatched",clientImp.getStatusForOrder(2));
        assertEquals("Delivered",clientImp.getStatusForOrder(3));
        assertEquals("Cancelled",clientImp.getStatusForOrder(4));

    }

    /**
     * using setAvailableFoodBoxFromServer to set
     * availableFoodBoxFromServer
     * test getItemIdsForFoodBox() using knowledge from
     * food_boxes.txt
     */
    @Test
    public void testGetItemIdsForFoodBox() {
        setAvailableFoodBoxFromServer();
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
    /**
     * set up availableFoodBoxFromServer using server
     * thus test getItemQuantityForFoodBox()
     */
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

    /**
     * using showFoodBox api to set availableFoodBoxFromServer()
     * from server
     */
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
     * set up availableFoodBoxFromServer using server
     * thus test getDietaryPreferenceForFoodBox()
     */
    @Test
    public void testGetDietaryPreferenceForFoodBox() {
        //when availableFoodBoxFromServer is null, this return null
        assertNull(clientImp.getDietaryPreferenceForFoodBox(0));
        setAvailableFoodBoxFromServer();
        assertEquals(clientImp.getDietaryPreferenceForFoodBox(1),"none");
        assertEquals(clientImp.getDietaryPreferenceForFoodBox(2),"pollotarian");
        //test invalid foodBoxId
        assertNull(clientImp.getDietaryPreferenceForFoodBox(9));
    }

    /**
     * set up availableFoodBoxFromServer using server
     * thus test getFoodBoxNumber()
     * there should be 5 available food box
     */
    @Test
    public void testGetFoodBoxNumber() {
        //no available food box when availableFoodBoxFromServer is null
        assertEquals(clientImp.getFoodBoxNumber(),0);
        setAvailableFoodBoxFromServer();
        assertEquals(clientImp.getFoodBoxNumber(),5);
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

    /**
     * check whether test placeOrder return true
     * and check whether the order is in server
     * by using requestStatus api
     */
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

        //place another order, as the interval was less than seven days, should return false
        setAvailableFoodBoxFromServer();
        clientImp.setCurrentFoodBox(clientImp.getAvailableFoodBoxFromServer().get(0));
        assertFalse(clientImp.placeOrder());

    }

    /**
     * test requestOrderStatus() when:
     * invalid orderNumber is given/ the is placed/
     * the order has been cancelled
     */
    @Test
    public void testRequestOrderStatus() {
        //test invalid orderNumber
        assertFalse(clientImp.requestOrderStatus(999));

        clientImp.setChi("0110121234");
        //register individual in server
        try {
            String request = "/registerShieldingIndividual?CHI=0110121234";
            ClientIO.doGETRequest(endpoint+request);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //set Caterer
        setAvailableFoodBoxFromServer();
        clientImp.setCurrentFoodBox(clientImp.getAvailableFoodBoxFromServer().get(0));
        clientImp.setIndividual(new ShieldingIndividualClientImp.Individual("Eric","K","EH7 5FL","123123"));
        ShieldingIndividualClientImp.Caterer c1;
        c1 = new ShieldingIndividualClientImp.Caterer("1", "c1", "EH7_5FL", 0);
        clientImp.setAvailableCatererFromServer(Collections.singletonList(c1));

        //register c1
        try {
            String request1 = "/registerCateringCompany?business_name=c1&postcode=EH7_5FL";
            ClientIO.doGETRequest(endpoint+request1);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //placeOrder()
        String orderId = null;
        try {
            String request = "/placeOrder?individual_id=0110121234" +
                    "&catering_business_name=c1&catering_postcode=EH7_5FL";
            String data = "{\"contents\": " + new Gson().toJson(clientImp.getCurrentFoodBox().getContents()) + "}";
            orderId = ClientIO.doPOSTRequest(endpoint+request,data);
            HashMap<String, ShieldingIndividualClientImp.Order> orders = new HashMap<>();
            orders.put(orderId, new ShieldingIndividualClientImp.Order(clientImp.getCurrentFoodBox(),0,0));
            clientImp.setOrders(orders);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(clientImp.requestOrderStatus(Integer.parseInt(orderId)));
        //orderStatus should == 0(placed)
        assertEquals(clientImp.getOrders().get(orderId).orderStatus,0);

        //cancel order
        try {
            String request = "/cancelOrder?order_id=" + orderId;
            ClientIO.doGETRequest(endpoint+request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //status should == 4 (canceled)
        assertTrue(clientImp.requestOrderStatus(Integer.parseInt(orderId)));
        assertEquals(clientImp.getOrders().get(orderId).orderStatus,4);
    }

    /**
     * using helper function to set orders
     * test getItemNameForOrder using valid/invalid inputs
     */
    @Test
    public void testGetItemNameForOrder() {
        setUpOrdersAppleOrangePork();

        assertEquals(clientImp.getItemNameForOrder(1,123),"apple");
        assertEquals(clientImp.getItemNameForOrder(2,123),"orange");
        assertEquals(clientImp.getItemNameForOrder(3,123),"pork");

        //invalid itemId
        assertNull(clientImp.getItemNameForOrder(4, 123));
        //invalid orderNumber
        assertNull(clientImp.getItemNameForOrder(1, 1233));

    }


    /**
     * set up clientImp.orders,
     * orders contain one order: order id = 123
     * the order contain 2 apple 2 orange 1 pork
     */
    private void setUpOrdersAppleOrangePork() {
        ShieldingIndividualClientImp.Content apple =
                new ShieldingIndividualClientImp.Content(1, "apple", 2);
        ShieldingIndividualClientImp.Content orange =
                new ShieldingIndividualClientImp.Content(2, "orange", 2);
        ShieldingIndividualClientImp.Content pork =
                new ShieldingIndividualClientImp.Content(3, "pork", 1);
        ShieldingIndividualClientImp.FoodBox f =
                new ShieldingIndividualClientImp.FoodBox(Arrays.asList(apple,orange,pork),"delivered_by","none","1","box a");

        ShieldingIndividualClientImp.Order o = new ShieldingIndividualClientImp.Order(f,0,1000);
        HashMap<String, ShieldingIndividualClientImp.Order> orders = new HashMap<>();
        orders.put("123",o);
        clientImp.setOrders(orders);
    }

    /**
     * using helper function to set orders
     *  test getItemQuantityForOrder() using both
     *  valid and invalid inputs
     */
    @Test
    public void testGetItemQuantityForOrder() {
        setUpOrdersAppleOrangePork();

        assertEquals(clientImp.getItemQuantityForOrder(1,123),2);
        assertEquals(clientImp.getItemQuantityForOrder(2,123),2);
        assertEquals(clientImp.getItemQuantityForOrder(3,123),1);

        //no itemId 4 is not in the food box
        assertEquals(clientImp.getItemQuantityForOrder(4,123),0);
        //no order: 1223 in orders
        assertEquals(clientImp.getItemQuantityForOrder(1,1223),0);
    }

    /**
     * check whether editedFoodBoxHashmap
     * change accordingly.
     */
    @Test
    public void testSetItemQuantityForOrder() {
        //set up both editedFoodBoxHashmap and orders
        ShieldingIndividualClientImp.Content apple =
                new ShieldingIndividualClientImp.Content(1, "apple", 2);
        ShieldingIndividualClientImp.Content orange =
                new ShieldingIndividualClientImp.Content(2, "orange", 2);
        ShieldingIndividualClientImp.Content pork =
                new ShieldingIndividualClientImp.Content(3, "pork", 1);
        ShieldingIndividualClientImp.FoodBox f =
                new ShieldingIndividualClientImp.FoodBox(Arrays.asList(apple,orange,pork),"delivered_by","none","1","box a");
        HashMap<String, ShieldingIndividualClientImp.FoodBox> m = new HashMap<>();
        //order number = 123
        m.put("123",f);
        setUpOrdersAppleOrangePork();
        clientImp.setEditedFoodBoxHashmap(m);

        assertTrue(clientImp.setItemQuantityForOrder(1,123,1));
        assertTrue(clientImp.setItemQuantityForOrder(1,123,2));
        //return false as 3 is greater than initial quantity
        assertFalse(clientImp.setItemQuantityForOrder(1,123,3));
        //return false as not item has item id 4
        assertFalse(clientImp.setItemQuantityForOrder(4,123,1));
        //return false no order has orderNumber: 1232
        assertFalse(clientImp.setItemQuantityForOrder(4,1232,1));
    }

    /**
     * set up individual, orders, editedFoodBoxHashmap
     * register individual, caterer, place the order in server
     * hence test editOrder()
     */
    @Test
    public void testEditOrder() {
        //test invalid orderNumber
        assertFalse(clientImp.editOrder(9999));

        clientImp.setChi("0110121234");
        //register individual in server
        try {
            String request = "/registerShieldingIndividual?CHI=0110121234";
            ClientIO.doGETRequest(endpoint+request);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //set Caterer
        setAvailableFoodBoxFromServer();
        clientImp.setCurrentFoodBox(clientImp.getAvailableFoodBoxFromServer().get(0));
        clientImp.setIndividual(new ShieldingIndividualClientImp.Individual("Eric","K","EH7 5FL","123123"));
        ShieldingIndividualClientImp.Caterer c1;
        c1 = new ShieldingIndividualClientImp.Caterer("1", "c1", "EH7_5FL", 0);
        clientImp.setAvailableCatererFromServer(Collections.singletonList(c1));

        //register c1
        try {
            String request1 = "/registerCateringCompany?business_name=c1&postcode=EH7_5FL";
            ClientIO.doGETRequest(endpoint+request1);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //placeOrder()
        String orderId = null;
        try {
            String request = "/placeOrder?individual_id=0110121234" +
                   "&catering_business_name=c1&catering_postcode=EH7_5FL";
            String data = "{\"contents\": " + new Gson().toJson(clientImp.getCurrentFoodBox().getContents()) + "}";
            orderId = ClientIO.doPOSTRequest(endpoint+request,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, ShieldingIndividualClientImp.Order> orders = new HashMap<>();
        orders.put(orderId, new ShieldingIndividualClientImp.Order(clientImp.getCurrentFoodBox(),0,0));
        clientImp.setOrders(orders);


        //change quantity in editedFoodBoxHashmap
        ShieldingIndividualClientImp.Content zeroCucumbers =
                new ShieldingIndividualClientImp.Content(1,"cucumbers",0);
        ShieldingIndividualClientImp.Content oneTomatoes =
                new ShieldingIndividualClientImp.Content(2,"tomatoes",1);
        ShieldingIndividualClientImp.Content onePork =
                new ShieldingIndividualClientImp.Content(6,"pork",1);
        List<ShieldingIndividualClientImp.Content> contents = Arrays.asList(zeroCucumbers,onePork,onePork);
        ShieldingIndividualClientImp.FoodBox newFoodBox = new ShieldingIndividualClientImp.FoodBox(contents,"catering","none","1","box a");
        //EFH: short for editFoodBoxHashmap
        HashMap<String, ShieldingIndividualClientImp.FoodBox> EFH = new HashMap<>();
        EFH.put(orderId,clientImp.getCurrentFoodBox());
        clientImp.setEditedFoodBoxHashmap(EFH);

        assertTrue(clientImp.editOrder(Integer.parseInt(orderId)));

    }

    /**
     * do what "placeOrder()" do,
     * and test CancelOrder()
     */
    @Test
    public void testCancelOrder() {
        //test inValid orderNumber
        assertFalse(clientImp.cancelOrder(999));

        clientImp.setChi("0110121234");
        //register individual in server
        try {
            String request = "/registerShieldingIndividual?CHI=0110121234";
            ClientIO.doGETRequest(endpoint+request);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //set Caterer
        setAvailableFoodBoxFromServer();
        clientImp.setCurrentFoodBox(clientImp.getAvailableFoodBoxFromServer().get(0));
        clientImp.setIndividual(new ShieldingIndividualClientImp.Individual("Eric","K","EH7 5FL","123123"));
        ShieldingIndividualClientImp.Caterer c1;
        c1 = new ShieldingIndividualClientImp.Caterer("1", "c1", "EH7_5FL", 0);
        clientImp.setAvailableCatererFromServer(Collections.singletonList(c1));

        //register c1
        try {
            String request1 = "/registerCateringCompany?business_name=c1&postcode=EH7_5FL";
            ClientIO.doGETRequest(endpoint+request1);
        }catch (Exception e) {
            e.printStackTrace();
        }
        //placeOrder()
        String orderId = null;
        try {
            String request = "/placeOrder?individual_id=0110121234" +
                    "&catering_business_name=c1&catering_postcode=EH7_5FL";
            String data = "{\"contents\": " + new Gson().toJson(clientImp.getCurrentFoodBox().getContents()) + "}";
            orderId = ClientIO.doPOSTRequest(endpoint+request,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<String, ShieldingIndividualClientImp.Order> orders = new HashMap<>();
        orders.put(orderId, new ShieldingIndividualClientImp.Order(clientImp.getCurrentFoodBox(),0,0));
        clientImp.setOrders(orders);

        assertTrue(clientImp.cancelOrder(Integer.parseInt(orderId)));

    }

}
