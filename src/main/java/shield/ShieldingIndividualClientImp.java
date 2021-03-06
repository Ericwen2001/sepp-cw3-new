package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {
    private final String endpoint;

    private HashMap<String, Order> orders = new HashMap<>();
    private HashMap<String, FoodBox> editedFoodBoxHashmap = new HashMap<>();
    private Individual individual;
    private String chi;
    private FoodBox currentFoodBox;
    //initialize in showFoodBox()
    private List<FoodBox> availableFoodBoxFromServer;
    private List<Caterer> availableCatererFromServer;

    public ShieldingIndividualClientImp(String endpoint) {
        this.endpoint = endpoint;
    }

    public HashMap<String, Order> getOrders() {
        return orders;
    }

    public void setOrders(HashMap<String, Order> o) {
        orders = o;
    }

    public HashMap<String, FoodBox> getEditedFoodBoxHashmap() {
        return editedFoodBoxHashmap;
    }

    public void setEditedFoodBoxHashmap(HashMap<String, FoodBox> editedFoodBoxHashmap) {
        this.editedFoodBoxHashmap = editedFoodBoxHashmap;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public String getChi() {
        return chi;
    }

    public void setChi(String chi) {
        this.chi = chi;
    }

    public List<FoodBox> getAvailableFoodBoxFromServer() {
        return availableFoodBoxFromServer;
    }

    public void setAvailableFoodBoxFromServer(List<FoodBox> availableFoodBoxFromServer) {
        this.availableFoodBoxFromServer = availableFoodBoxFromServer;
    }

    public List<Caterer> getAvailableCatererFromServer() {
        return availableCatererFromServer;
    }

    public void setAvailableCatererFromServer(List<Caterer> availableCatererFromServer) {
        this.availableCatererFromServer = availableCatererFromServer;
    }

    public FoodBox getCurrentFoodBox() {
        return currentFoodBox;
    }

    public void setCurrentFoodBox(FoodBox currentFoodBox) {
        this.currentFoodBox = currentFoodBox;
    }

    /**
     * @param CHI chi number
     * @throws IllegalArgumentException if CHI is not valid
     */
    private void checkValidCHI(String CHI) {
        boolean condition1 = CHI.length() == 10;
        String regex = "[0-9]+";
        boolean condition2 = CHI.matches(regex);
        if (condition1 && condition2) {
            if (isValidDate(CHI.substring(0, 6))) {
                return;
            }
        }
        throw new IllegalArgumentException("CHI is not valid");
    }

    /**
     * check values for day, month, year can indeed be for a date.
     *
     * @param date input date
     * @return whether the date is valid.
     */
    private boolean isValidDate(String date) {
        int DD = Integer.parseInt(date.substring(4));
        int MM = Integer.parseInt(date.substring(2, 4));
        int YY = Integer.parseInt(date.substring(0, 2));
        List<Integer> longMonths = Arrays.asList(1, 3, 5, 7, 8, 10, 12);
        List<Integer> shortMonths = Arrays.asList(4, 6, 9, 11);
        List<Integer> allMonths = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        boolean leapYear = YY % 4 == 0;
        if (YY == 0) {//2000 is not a Leap year.
            leapYear = false;
        }
        boolean Feb29 = (leapYear && (DD == 29) && (MM == 2));

        if (DD == 31) {
            return longMonths.contains(MM);
        } else if (DD == 30) {
            return longMonths.contains(MM) || shortMonths.contains(MM);
        } else if (DD == 29) {
            return longMonths.contains(MM) || shortMonths.contains(MM) || Feb29;
        } else if (DD < 29) {
            return (allMonths.contains(MM));
        }
        return false;
    }

    /**
     * Returns true if CHI is valid and
     * the servers return "already registered"
     * or valid personal information. If operation
     * occurred correctly, individual is initialized.
     *
     * @param CHI CHI number of the shiedling individual
     * @return true if the operation occurred correctly
     */
    @Override
    public boolean registerShieldingIndividual(String CHI) {
        try {//check valid input
            checkValidCHI(CHI);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String request = "/registerShieldingIndividual?CHI=" + CHI;
        String result;
        try {
            result = ClientIO.doGETRequest(endpoint + request);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (result.equals("already registered")) {
            return true;
        }
        try {
            String[] splitResult = result.split(",");
            assert splitResult.length == 4;
            String postcode = splitResult[0].substring(2, splitResult[0].length() - 1);
            String name = splitResult[1].substring(1, splitResult[1].length() - 1);
            String surName = splitResult[2].substring(1, splitResult[2].length() - 1);
            String phoneNumber = splitResult[3].substring(1, splitResult[3].length() - 2);

            Objects.requireNonNull(postcode);
            Objects.requireNonNull(name);
            Objects.requireNonNull(surName);
            Objects.requireNonNull(phoneNumber);
            individual = new Individual(name, surName, postcode, phoneNumber);
            chi = CHI;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Initialize/update availableFoodBoxFromServer and
     * return id of foodboxes that satisfy given preference
     * if operations occurs correctly.
     *
     * @param dietaryPreference preference
     * @return id of foodboxes that satisfy given preference.
     */
    @Override
    public Collection<String> showFoodBoxes(String dietaryPreference) {
        // construct the endpoint request
        String request = "/showFoodBox?orderOption=catering&dietaryPreference=" + dietaryPreference;
        String requestAllPreference = "/showFoodBox?orderOption=catering&dietaryPreference=";
        // setup the response recepient
        List<FoodBox> responseBoxes;

        List<String> boxIds = new ArrayList<>();

        try {
            // perform request
            String response = ClientIO.doGETRequest(endpoint + request);
            String responseAll = ClientIO.doGETRequest(endpoint + requestAllPreference);
            // unmarshal cyresponse
            Type listType = new TypeToken<List<FoodBox>>() {
            }.getType();
            responseBoxes = new Gson().fromJson(response, listType);
            availableFoodBoxFromServer = new Gson().fromJson(responseAll, listType);
            // gather required fields
            for (FoodBox b : responseBoxes) {
                boxIds.add(b.id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return boxIds;
    }

    /**
     * Use currentFoodBox.contents to place order,
     * if success, set currentFoodBox to null,
     * store order details including time, order number,
     * status, contents.
     * Can't place an order if last order was placed within
     * seven days.
     *
     * @return true if Order is placed successfully.
     */
    // **UPDATE2** REMOVED PARAMETER
    @Override
    public boolean placeOrder() {
        //check last order date!
        ArrayList<Long> historicalOrderTime = new ArrayList<>();
        historicalOrderTime.add(0L);
        for (Order o : orders.values()) {
            if (o.orderStatus != 4) {//not cancelled orders
                historicalOrderTime.add(o.placedTime);
            }
        }
        long sevenDayMillis = 604800000;
        if (System.currentTimeMillis() - Collections.max(historicalOrderTime) < sevenDayMillis) {
            try {
                throw new Exception("less than seven days since last order");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        //access server
        String closestCompany = getClosestCateringCompany();
        String request = "/placeOrder?individual_id=" + chi +
                "&catering_business_name=" + closestCompany + "&catering_postcode="
                + closestCateringPostCode(closestCompany);

        String data = "{\"contents\": " + new Gson().toJson(currentFoodBox.contents) + "}";

        String orderNumber;
        try {
            orderNumber = ClientIO.doPOSTRequest(endpoint + request, data);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        orders.put(orderNumber, new Order(currentFoodBox, 0, System.currentTimeMillis()));
        editedFoodBoxHashmap.put(orderNumber, currentFoodBox);
        currentFoodBox = null;
        return true;
    }

    /**
     * A helper function of placeOrder()
     *
     * @param closestCompany name of a company
     * @return postcode of given company
     */
    private String closestCateringPostCode(String closestCompany) {
        for (Caterer c : availableCatererFromServer) {
            if (c.name.equals(closestCompany)) {
                return c.postCode;
            }
        }
        return null;
    }

    /**
     * use contents store in foodBoxHashMap to post request,
     * if success, update the contents in orders,
     * if fail, reset the contents in foodBoxHashMap
     *
     * @param orderNumber the order number
     * @return true if operation occurs correctly
     */
    @Override
    public boolean editOrder(int orderNumber) {
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new Exception("Don't have permission to access other's order status.");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        String request = "/editOrder?order_id=" + orderNumber;
        List<Content> rawData = editedFoodBoxHashmap.get(String.valueOf(orderNumber)).contents;
        String data = "{\"contents\": " + new Gson().toJson(rawData) + "}";
        String result = "False";
        try {
            result = ClientIO.doPOSTRequest(endpoint + request, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orderID = String.valueOf(orderNumber);
        Order o = orders.get(orderID);
        if (result.equals("True")) {//update the contents in orders
            o.foodBox = editedFoodBoxHashmap.get(orderID);
            orders.put(orderID, o);
            return true;
        } else {//reset the contents in foodBoxHashMap
            editedFoodBoxHashmap.put(orderID, o.foodBox);
        }

        return false;
    }

    @Override
    public boolean cancelOrder(int orderNumber) {
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new Exception("No permission to cancel this order!");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        String request = "/cancelOrder?order_id=" + orderNumber;
        try {
            String result = ClientIO.doGETRequest(endpoint + request);
            return result.equals("True");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * A helper function check wheth
     * orders contain given orderNumber
     *
     * @param orderNumber order number
     * @return true if "orders" contain this order
     */
    private boolean doNotHasThisOrder(int orderNumber) {
        return !orders.containsKey(String.valueOf(orderNumber));
    }

    /**
     * check whether the order belong to this user
     * if yes, update the status of this order.
     *
     * @param orderNumber the order number
     * @return true if the status is updated
     */
    @Override
    public boolean requestOrderStatus(int orderNumber) {
        //check whether the order belong to this user
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new Exception("Don't have permission to access other's order status.");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        String request = "/requestStatus?order_id=" + orderNumber;

        try {
            String response = ClientIO.doGETRequest(endpoint + request);
            int status = Integer.parseInt(response);
            Order o = orders.get(String.valueOf(orderNumber));
            o.orderStatus = status;
            orders.put(String.valueOf(orderNumber), o);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * convert the format of caterers' information to List<caterer>
     *
     * @param response response of /getCaterers' from server
     * @return list of caterer
     */
    private List<Caterer> stringToCaterers(String response) {
        List<Caterer> caterers = new ArrayList<>();
        String[] splitS = response.split("\",\"");

        // remove " [" " and " "] "
        splitS[0] = splitS[0].substring(2);
        splitS[splitS.length - 1] = splitS[splitS.length - 1].substring(0, splitS[splitS.length - 1].length() - 2);

        for (String i : splitS) {
            String[] catererInfo = i.split(",");
            if (catererInfo.length != 3) {
                System.out.println("Warning: one input line don't have a correct caterer format," +
                        "skip to next caterer");
                continue;
            }
            float distance = getDistance(individual.postCode, catererInfo[2]);
            caterers.add(new Caterer(catererInfo[0], catererInfo[1], catererInfo[2], distance));
        }
        return caterers;
    }

    /**
     * Initialize/update availableCatererFromServer
     * from server and return their information in String.
     * return null if not available server.
     *
     * @return a String contain the name and postcode of all caterers
     */
    // **UPDATE**
    @Override
    public Collection<String> getCateringCompanies() {
        String request = "/getCaterers";
        String response;
        Collection<String> res = new ArrayList<>();
        try {
            response = ClientIO.doGETRequest(endpoint + request);

            availableCatererFromServer = stringToCaterers(response);
            for (Caterer i : availableCatererFromServer) {
                res.add("[");
                res.add(i.name);
                res.add(",");
                res.add(i.postCode);
                res.add("]");
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Using server's api to get the distance of two post codes,
     * return -1 if there are exceptions.
     *
     * @param postCode1 post code of one location
     * @param postCode2 post code of another location
     * @return the distance between two post codes.
     */
    // **UPDATE**
    @Override
    public float getDistance(String postCode1, String postCode2) {
        Objects.requireNonNull(postCode1);
        Objects.requireNonNull(postCode2);
        //Convert to correct format
        postCode1 = postCode1.replace(' ', '_');
        postCode2 = postCode2.replace(' ', '_');

        String request = "/distance?postcode1=" + postCode1 + "&postcode2=" + postCode2;
        String response;

        try {
            response = ClientIO.doGETRequest(endpoint + request);
            return Float.parseFloat(response);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @return whether the user using this client is registered.
     */
    @Override
    public boolean isRegistered() {
        return individual != null;
    }

    @Override
    public String getCHI() {
        try {
            if (!isRegistered()) {
                throw new Exception("Not register yet!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return chi;
    }

    /**
     * return number of available food box,
     * if haven't get food box info yet, return 0
     *
     * @return number of available food box
     */
    @Override
    public int getFoodBoxNumber() {
        try {
            return availableFoodBoxFromServer.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public String getDietaryPreferenceForFoodBox(int foodBoxId) {
        if (availableFoodBoxFromServer == null) {
            try {
                throw new Exception("Please run showFoodBoxes first!");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        for (FoodBox b : availableFoodBoxFromServer) {
            if (Integer.parseInt(b.id) == foodBoxId) {
                return b.diet;
            }
        }
        return null;
    }

    @Override
    public int getItemsNumberForFoodBox(int foodBoxId) {
        if (availableFoodBoxFromServer == null) {
            try {
                throw new Exception("Please run showFoodBoxes first!");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        int itemNumber = 0;
        for (FoodBox b : availableFoodBoxFromServer) {
            if (Integer.parseInt(b.id) == foodBoxId) {
                for (Content c : b.contents) {
                    itemNumber += c.quantity;
                }
                return itemNumber;
            }
        }

        return 0;
    }

    /**
     * @param foodboxId id of the food box
     * @return collection of Item Ids for the foodBox
     */
    @Override
    public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
        if (availableFoodBoxFromServer == null) {
            try {
                throw new Exception("Please run showFoodBoxes first!");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        ArrayList<Integer> itemId = new ArrayList<>();
        for (FoodBox b : availableFoodBoxFromServer) {
            if (Integer.parseInt(b.id) == foodboxId) {
                for (Content c : b.contents) {
                    itemId.add(c.id);
                }
                return itemId;
            }
        }
        return null;
    }

    /**
     * return null if no such item.
     *
     * @param itemId    the id of the item
     * @param foodBoxId the food box id
     * @return the name of the item
     */
    @Override
    public String getItemNameForFoodBox(int itemId, int foodBoxId) {
        if (availableFoodBoxFromServer == null) {
            try {
                throw new Exception("Please run showFoodBoxes first!");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        for (FoodBox b : availableFoodBoxFromServer) {
            if (Integer.parseInt(b.id) == foodBoxId) {
                for (Content c : b.contents) {
                    if (itemId == c.id) {
                        return c.name;
                    }
                }
            }
        }
        return null;
    }

    /**
     * return 0 if no such item.
     *
     * @param itemId    the id of the item
     * @param foodBoxId the food box id
     * @return the quantity of the item
     */
    @Override
    public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
        if (availableFoodBoxFromServer == null) {
            try {
                throw new Exception("Please run showFoodBoxes first!");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        for (FoodBox b : availableFoodBoxFromServer) {
            if (Integer.parseInt(b.id) == foodBoxId) {
                for (Content c : b.contents) {
                    if (itemId == c.id) {
                        return c.quantity;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * initialize/update currentFoodBox. return false if availableFoodBookFromServer
     * is null/foodBoxId is not valid.
     *
     * @param foodBoxId the food box id as last returned from the server
     * @return true if currentFoodBox is initialized/updated.
     */
    @Override
    public boolean pickFoodBox(int foodBoxId) {
        if (availableFoodBoxFromServer == null) {
            try {
                throw new Exception("please run showFoodBoxes() first to get information from the server");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        for (FoodBox i : availableFoodBoxFromServer) {
            if (Integer.parseInt(i.id) == foodBoxId) {
                currentFoodBox = i;
                return true;
            }
        }
        try {
            throw new IllegalArgumentException("no such foodBox id!");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
        if (currentFoodBox == null) {
            try {
                throw new Exception("Haven't pick a food box yet!");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        if (quantity < 0) {
            try {
                throw new Exception("Invalid quantity");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        for (Content c : currentFoodBox.contents) {
            if (itemId == c.id) {
                c.quantity = quantity;
                return true;
            }
        }

        try {
            throw new Exception("itemId " + itemId + " is not found!");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * return empty collection if no orders yet
     *
     * @return all order numbers from "orders"
     */
    @Override
    public Collection<Integer> getOrderNumbers() {
        Collection<Integer> orderNumbers = new ArrayList<>();
        for (String s : orders.keySet()) {
            orderNumbers.add(Integer.parseInt(s));
        }
        return orderNumbers;
    }

    /**
     * return the status of selected order,
     * if the status is incognizant, output
     * "unknown status!" If orderuNmber not valid,
     * return null;
     *
     * @param orderNumber the order number
     * @return the status of the order
     */
    @Override
    public String getStatusForOrder(int orderNumber) {
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new IllegalArgumentException("Don't has this order");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }

        }
        int orderStatus = orders.get(String.valueOf(orderNumber)).orderStatus;

        if (orderStatus == -1) {
            try {
                throw new IllegalArgumentException("invalid orderNumber: " + orderNumber);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        }
        return switch (orderStatus) {
            case 0 -> "Placed";
            case 1 -> "Packed";
            case 2 -> "Dispatched";
            case 3 -> "Delivered";
            case 4 -> "Cancelled";
            default -> "unknown status!";
        };
    }

    /**
     * return null if the order is not in "orders"
     *
     * @param orderNumber the order number
     * @return collections of item id in the content
     */
    @Override
    public Collection<Integer> getItemIdsForOrder(int orderNumber) {
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new Exception("Do not have permission to access this order!");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        ArrayList<Integer> ItemIds = new ArrayList<>();
        Order o = orders.get(String.valueOf(orderNumber));
        for (Content c : o.foodBox.contents) {
            ItemIds.add(c.id);
        }
        return ItemIds;
    }

    /**
     * @param itemId      the food box id as last returned from the server
     * @param orderNumber the order number
     * @return name of the item
     */
    @Override
    public String getItemNameForOrder(int itemId, int orderNumber) {
        //check whether orderNumber is valid
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new Exception("Do not have permission to access this order!");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        Order o = orders.get(String.valueOf(orderNumber));
        for (Content c : o.foodBox.contents) {
            if (c.id == itemId) {
                return c.name;
            }
        }
        return null;
    }

    @Override
    public int getItemQuantityForOrder(int itemId, int orderNumber) {
        //check whether orderNumber is valid
        if (doNotHasThisOrder(orderNumber)) {
            try {
                throw new Exception("Do not have permission to access this order!");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        Order o = orders.get(String.valueOf(orderNumber));
        for (Content c : o.foodBox.contents) {
            if (c.id == itemId) {
                return c.quantity;
            }
        }
        return 0;
    }

    /**
     * set item Quantity for an order belong to this user.
     * set the content quantity in editedFoodBoxHashmap
     * if quantity <= the quantity of this item in the server then
     * change quantity in editedFoodBoxHashmap and return true
     * do this before editOrder()
     * if editOrder() return true, alter orders using editedFoodBoxHashmap
     * if editOrder() return false, alter editedFoodBoxHashmap using orders
     *
     * @param itemId      the food box id as last returned from the server
     * @param orderNumber the order number
     * @param quantity    the food box item quantity to be set
     * @return true if operation occurs correctly.
     */
    @Override
    public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
        if (doNotHasThisOrder(orderNumber)) {
            try {

                throw new Exception("No this order!");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        //change the item quantity for that order in foodBoxHashMap
        FoodBox foodBox = editedFoodBoxHashmap.get(String.valueOf(orderNumber));
        int currentQuantityInServer = getItemQuantityForOrder(itemId, orderNumber);
        for (Content c : foodBox.contents) {
            if (c.id == itemId) {
                if (quantity <= currentQuantityInServer) {
                    c.quantity = quantity;
                    editedFoodBoxHashmap.put(String.valueOf(orderNumber), foodBox);
                    return true;
                }
            }
        }
        try {
            throw new Exception("Not such itemID: " + itemId + " in the order!");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // **UPDATE**
    @Override
    public String getClosestCateringCompany() {
        if (availableCatererFromServer == null) {
            try {
                throw new Exception("Please run getCateringCompanies() first!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (availableCatererFromServer.isEmpty()) {
            try {
                throw new Exception("No available catering company! Try getCateringCompanies() to update");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ArrayList<Float> allDistance = new ArrayList<>();
        for (Caterer i : availableCatererFromServer) {
            allDistance.add(i.distance);
        }
        float min = Collections.min(allDistance);

        int minIndex = allDistance.indexOf(min);

        return availableCatererFromServer.get(minIndex).name;
    }

    /**
     * "Individual" holding the information passed by the server.
     */
    static class Individual {
        private final String name;
        private final String surName;
        private final String phoneNumber;
        private String postCode;
        public Individual(String name, String surName, String postCode, String phoneNumber) {
            this.name = name;
            this.surName = surName;
            this.postCode = postCode;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getSurName() {
            return surName;
        }

        public String getPostCode() {
            return postCode;
        }

        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }
    }

    static class FoodBox {

        // a field marked as transient is skipped in marshalling/unmarshalling
        private final List<Content> contents;
        private final String delivered_by;
        private final String diet;
        private final String id;
        private final String name;
        public FoodBox(List<Content> contents, String delivered_by, String diet, String id, String name) {
            this.contents = contents;
            this.delivered_by = delivered_by;
            this.diet = diet;
            this.id = id;
            this.name = name;
        }

        public List<Content> getContents() {
            return contents;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }


    }

    static class Content {
        private final int id;
        private final String name;
        private int quantity;

        public Content(int id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    static class Order {
        FoodBox foodBox;
        int orderStatus;
        long placedTime;

        public Order(FoodBox foodBox, int orderStatus, long placedTime) {
            this.foodBox = foodBox;
            this.orderStatus = orderStatus;
            this.placedTime = placedTime;
        }
    }

    // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

    static class Caterer {
        String index;
        String name;
        String postCode;
        float distance;

        public Caterer(String index, String name, String postCode, float distance) {
            this.index = index;
            this.name = name;
            this.postCode = postCode;
            this.distance = distance;
        }
    }
}
