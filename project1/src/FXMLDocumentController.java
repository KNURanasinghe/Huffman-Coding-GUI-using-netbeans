import java.io.*;
import java.util.*;

class HuffmanTree implements Serializable {
    private static final long serialVersionUID = 1L; // Added serialVersionUID

    private static class Node implements Serializable {
        private static final long serialVersionUID = 1L; // Added serialVersionUID

        char character;
        int frequency;
        int label;
        Node left;
        Node right;

        Node(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
            this.left = null;
            this.right = null;
        }

        String getCharacterAsString() {
            return character == '\0' ? "none" : String.valueOf(character);
        }
    }

    private Node root;
    private int labelCount;

    HuffmanTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.frequency - b.frequency);

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node('\0', left.frequency + right.frequency); // Combine frequencies
            parent.left = left;
            parent.right = right;
            pq.add(parent);
        }

        root = pq.poll();
        labelCount = 1;
        label(root);
    }

    HuffmanTree() {
        root = null;
        labelCount = 1;
    }

    public void insert(Node newNode) {
        if (root == null) {
            root = newNode;
            return;
        }

        Node current = root;
        Node parent = null;
        while (true) {
            parent = current;
            if (newNode.label < current.label) {
                current = current.left;
                if (current == null) {
                    parent.left = newNode;
                    return;
                }
            } else {
                current = current.right;
                if (current == null) {
                    parent.right = newNode;
                    return;
                }
            }
        }
    }

    private int label(Node node) {
        if (node != null) {
            int next = label(node.left);
            node.label = labelCount++;
            next = label(node.right);
            return next;
        }
        return labelCount;
    }

    public void save(String fileName) {
        try (Formatter formatter = new Formatter(fileName)) {
            System.out.println("Saving Huffman tree to file: " + fileName);
            save(root, formatter);
            System.out.println("Huffman tree saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving Huffman tree to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void save(Node node, Formatter formatter) {
        if (node != null) {
            System.out.println("Saving node: " + node.label + " " + node.getCharacterAsString() + " " + node.frequency);
            formatter.format("%d %s %d%n", node.label, node.getCharacterAsString(), node.frequency);
            save(node.left, formatter);
            save(node.right, formatter);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(0, "root:", root, sb);
        return sb.toString();
    }

    private void toString(int level, String n, Node node, StringBuilder sb) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < level; i++) {
            sb.append("  ");
        }

        if (node.character != '\0') {
            sb.append(String.format("%s <%c, %d>%n", n, node.character, node.frequency));
        } else {
            sb.append(String.format("%s <%d>%n", n, node.frequency));
        }

        if (node.left != null || node.right != null) {
            toString(level + 1, "left: ", node.left, sb);
            toString(level + 1, "right:", node.right, sb);
        }
    }

    private void generateCodesHelper(Node root, String code, Map<Character, String> codes) {
        if (root == null) return;
        if (root.left == null && root.right == null) {
            codes.put(root.character, code);
            return;
        }
        generateCodesHelper(root.left, code + "0", codes);
        generateCodesHelper(root.right, code + "1", codes);
    }

    public Map<Character, String> generateCodes() {
        Map<Character, String> codes = new HashMap<>();
        generateCodesHelper(root, "", codes);
        return codes;
    }

    public String encode(String message) {
        StringBuilder encoded = new StringBuilder();
        Map<Character, String> codes = generateCodes();
        for (char c : message.toCharArray()) {
            encoded.append(codes.get(c));
        }
        return encoded.toString();
    }

    public String decode(String encodedMessage) {
        StringBuilder decoded = new StringBuilder();
        Node current = root;
        for (char c : encodedMessage.toCharArray()) {
            if (current == null) {
                decoded.append('?');
                continue;
            }
            current = (c == '0') ? current.left : current.right;
            if (current != null && current.left == null && current.right == null) {
                decoded.append(current.character);
                current = root;
            }
        }
        return decoded.toString();
    }

    public void serializeTreeToFile(String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(root);
        } catch (IOException e) {
            System.err.println("Error serializing Huffman tree to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static HuffmanTree deserializeTreeFromFile(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Node root = (Node) ois.readObject();
            HuffmanTree tree = new HuffmanTree();
            tree.root = root;
            System.out.println("Huffman tree deserialized successfully from file: " + fileName);
            return tree;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error deserializing Huffman tree from file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Node buildHuffmanTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.frequency - b.frequency);

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node('\0', left.frequency + right.frequency); // Combine frequencies
            parent.left = left;
            parent.right = right;
            pq.add(parent);
        }

        return pq.poll();
    }

    public HuffmanTree(String fileName) {
        root = null; // Initialize root to null
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            Map<Character, Integer> frequencyMap = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                char character = parts[1].charAt(0);
                int frequency = Integer.parseInt(parts[2]);
                frequencyMap.put(character, frequency);
            }
            // Construct the Huffman tree using the frequency map
            this.root = buildHuffmanTree(frequencyMap);
            System.out.println("Huffman tree loaded successfully from file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error loading Huffman tree from file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Test your code here if needed
    }
}
