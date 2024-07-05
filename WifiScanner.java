import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class WifiNetwork {
    String ssid;
    String security;

    WifiNetwork(String ssid, String security) {
        this.ssid = ssid;
        this.security = security;
    }

    @Override
    public String toString() {
        return String.format("%-30s %-20s", ssid, security);
    }
}

public class WifiScanner {

    public static List<WifiNetwork> listWifiNetworks() throws IOException {
        List<WifiNetwork> wifiNetworks = new ArrayList<>();

        // Execute the command to list WiFi networks
        ProcessBuilder builder = new ProcessBuilder("netsh", "wlan", "show", "networks", "mode=Bssid");
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            String ssid = null, security = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("SSID")) {
                    ssid = line.split(":")[1].trim();
                } else if (line.trim().startsWith("Authentication")) {
                    security = line.split(":")[1].trim();
                }

                if (ssid != null && security != null) {
                    wifiNetworks.add(new WifiNetwork(ssid, security));
                    ssid = null;
                    security = null;
                }
            }
        }

        return wifiNetworks;
    }

    public static List<WifiNetwork> filterWifiNetworks(List<WifiNetwork> networks, String filterType) {
        List<WifiNetwork> filteredNetworks = new ArrayList<>();
        for (WifiNetwork network : networks) {
            if (filterType.equalsIgnoreCase("U") && network.security.contains("Open")) {
                filteredNetworks.add(network);
            } else if (filterType.equalsIgnoreCase("L") && !network.security.contains("Open")) {
                filteredNetworks.add(network);
            }
        }
        return filteredNetworks;
    }

    public static void main(String[] args) {
        try {
            List<WifiNetwork> networks = listWifiNetworks();

            if (networks.isEmpty()) {
                System.out.println("No WiFi networks found.");
                return;
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter L for WiFi networks that need a password, U for WiFi networks that do not need a password, or A to list all networks: ");
            String filterType = scanner.nextLine().trim().toUpperCase();

            if (!filterType.equals("L") && !filterType.equals("U") && !filterType.equals("A")) {
                System.out.println("Invalid input. Please enter 'L', 'U', or 'A'.");
                return;
            }

            List<WifiNetwork> filteredNetworks = filterType.equals("A") ? networks : filterWifiNetworks(networks, filterType);

            if (filteredNetworks.isEmpty()) {
                System.out.println("No matching WiFi networks found.");
            } else {
                System.out.printf("%-30s %-20s%n", "SSID", "Security");
                System.out.println("=".repeat(50));
                for (WifiNetwork network : filteredNetworks) {
                    System.out.println(network);
                }
            }

        } catch (IOException e) {
            System.err.println("Error listing WiFi networks: " + e.getMessage());
        }
    }
}
