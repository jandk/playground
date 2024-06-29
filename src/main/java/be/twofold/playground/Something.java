package be.twofold.playground;

import java.util.*;

public class Something {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            int choice;
            System.out.println("Choose one of the next options:");
            System.out.println("1. Add Client");
            System.out.println("2. Add Destination");
            System.out.println("3. Create new shipment");
            System.out.println("4. Update an existing shipment");
            System.out.println("5. Sending/Receiving shipment");
            System.out.println("6. Report1 – List the existing clients");
            System.out.println("7. Report2 – List the destination details");
            System.out.println("8. Report3 – List the shipment details");
            System.out.println("9. Report4 – List the queued shipments");
            System.out.println("10. Report5 – List the client shipments");
            System.out.println("11. Report6 – List the income");
            System.out.println("12. Report7 – List the shipments not yet collected");
            System.out.println("13. Save and Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();


            switch (choice) {
                case 1:
                    addClient();
                    break;
                case 2:
                    addDestination();
                    break;
                case 3:
                    createNewShipment();
                    break;
                case 4:
                    updateExistingShipment();
                    break;
                case 5:
                    manageShipmentDates();
                    break;
                case 6:
                    listAllClients();
                    break;
                case 7:
                    listDestinationDetails();
                    break;
                case 8:
                    listShipmentDetails();
                    break;
                case 9:
                    listQueuedShipments();
                    break;
                case 10:
                    listClientShipments();
                    break;
                case 11:
                    listTotalIncome();
                    break;
                case 12:
                    listShipmentsNotYetCollected();
                    break;
                case 13:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 13.");
            }
        }
        scanner.close();
    }

    private static void addClient() {
    }

    private static void addDestination() {
    }

    private static void createNewShipment() {
    }

    private static void updateExistingShipment() {
    }

    private static void manageShipmentDates() {
    }

    private static void listAllClients() {
    }

    private static void listDestinationDetails() {
    }

    private static void listShipmentDetails() {
    }

    private static void listQueuedShipments() {
    }

    private static void listClientShipments() {
    }

    private static void listTotalIncome() {
    }

    private static void listShipmentsNotYetCollected() {
    }
}
