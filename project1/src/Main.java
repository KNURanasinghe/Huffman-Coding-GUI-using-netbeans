import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Formatter;
import java.io.File;

class Node implements java.io.Serializable {
    char character;
    int frequency;
    int label;
    Node left, right;

    Node(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        left = right = null;
    }

    Node(int label, String s, int frequency) {
        this.label = label;
        if (s.equals("none")) {
            character = '\0';
        } else if (s.equals("space")) {
            character = ' ';
        } else {
            character = s.charAt(0);
        }
        this.frequency = frequency;
        left = right = null;
    }

    public String getCharacterAsString() {
        if (character == '\0') {
            return "none";
        } else if (character == ' ') {
            return "space";
        } else {
            return Character.toString(character);
        }
    }
}

class HuffmanTree {
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

    public void serializeTreeToFile(String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(root);
        }
    }

    public static HuffmanTree deserializeTreeFromFile(String fileName) throws IOException, ClassNotFoundException {
        Node root;
        try (FileInputStream fis = new FileInputStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            root = (Node) ois.readObject();
        }
        HuffmanTree tree = new HuffmanTree(Collections.emptyMap());
        tree.root = root;
        return tree;
    }
}

public class Main {
    private static frame1 gui;
    static class Wrapper<T> {
        T value;

        Wrapper(T value) {
            this.value = value;
        }
    }

    private static Wrapper<HuffmanTree> huffmanTreeWrapper = new Wrapper<>(null);
    
    

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            gui = new frame1(huffmanTreeWrapper);
            
            gui.setVisible(true);
            java.awt.EventQueue.invokeLater(() -> {

                gui.getEncodeButton().addActionListener((ActionEvent evt) -> {
                    String inputMessage = gui.getPlainText();
                    Map<Character, Integer> frequencyMap = getFrequencyMap(inputMessage);
                    if(inputMessage.length()==0){
                        gui.setOutputText("enter a plain text string to be encoded");
                    }else{
                         gui.setOutputText("");
                    }
                    HuffmanTree huffmanTree = new HuffmanTree(frequencyMap);
                    String encodedMessage = huffmanTree.encode(inputMessage);
                    gui.setEncodedText(encodedMessage);
                     System.out.print("input msg"+ inputMessage);
                    huffmanTreeWrapper.value = huffmanTree;
                    System.out.print(encodedMessage);
                });
               

                gui.getFrequencyButton().addActionListener((ActionEvent evt) -> {
                    String inputMessage = gui.getPlainText();
                    Map<Character, Integer> frequencyMap = getFrequencyMap(inputMessage);
                    gui.setFrequencyText(getFrequencyText(frequencyMap));
                });

                gui.getDisplayHuffmanTreeButton().addActionListener((ActionEvent evt) -> {
                    if (huffmanTreeWrapper.value != null) {
                        String huffmanTreeString = huffmanTreeWrapper.value.toString();
                        gui.setOutputText(huffmanTreeString);
                    } else {
                        gui.setOutputText("Please enter a message to display the Huffman tree.");
                    }
                });

                gui.getDecodeButton().addActionListener((ActionEvent evt) -> {
                    if (huffmanTreeWrapper.value != null) {
                        String encodedMessage = gui.getEncodedText();
                        String decodedMessage = huffmanTreeWrapper.value.decode(encodedMessage);
                     
                        gui.setPlainText(decodedMessage);
                        System.out.print(decodedMessage);
                    } else {
                        gui.setPlainText("Please encode a message first.");
                    }
                    
                });

                gui.getSaveHuffmanButton().addActionListener((ActionEvent evt) -> {
                    saveHuffmanTreeToFile();
                });
            });
        });
    }

    public static void saveHuffmanTreeToFile() {
        String fileName = "a/tree.txt";//to save the file add the path here otherwise saving is not work
        if (huffmanTreeWrapper.value != null) {
            try {
                huffmanTreeWrapper.value.save(fileName);
                gui.setOutputText("Huffman tree saved to " + fileName);
            } catch (Exception e) {
                gui.setOutputText("Error: Failed to save Huffman tree to file");
                e.printStackTrace();
            }
        } else {
            gui.setOutputText("Please generate a Huffman tree first.");
        }
    }

    private static Map<Character, Integer> getFrequencyMap(String message) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : message.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
        return frequencyMap;
    }

    private static String getFrequencyText(Map<Character, Integer> frequencyMap) {
        StringBuilder frequencyText = new StringBuilder("Character Frequency:\n");
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            frequencyText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return frequencyText.toString();
    }
}
