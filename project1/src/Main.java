import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


class Node implements java.io.Serializable {
    char character;
    Node left, right;

    Node(char character, int frequency) {
        this.character = character;
        left = right = null;
    }

    Node(int label, String s, int frequency) {
        if (s.equals("none")) {
            character = '\0';
        } else if (s.equals("space")) {
            character = ' ';
        } else {
            character = s.charAt(0);
        }
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
        String fileName = "C:\\Users\\yasit\\Desktop\\project1\\src\\tree.txt"; // Adjust the file path as needed
        try {
            Map<Character, Integer> frequencyMap = readFrequencyMapFromFile(fileName);
            HuffmanTree huffmanTree = new HuffmanTree(frequencyMap);
            System.out.println(huffmanTree);
            huffmanTreeWrapper.value = huffmanTree;
        } catch (IOException e) {
            e.printStackTrace();
        }

        java.awt.EventQueue.invokeLater(() -> {
            gui = new frame1(huffmanTreeWrapper);
            gui.setVisible(true);
            
            gui.getLoadHuffmanButton().addActionListener((java.awt.event.ActionEvent evt) -> {
                loadHuffmanTreeFromFile();
            });
            
            gui.getEncodeButton().addActionListener((ActionEvent evt) -> {
                String inputMessage = gui.getPlainText();
                Map<Character, Integer> frequencyMapEncode = getFrequencyMap(inputMessage);
                if (inputMessage.length() == 0) {
                    gui.setOutputText("Enter a plain text string to be encoded");
                } else {
                    gui.setOutputText("");
                }
                HuffmanTree huffmanTreeEncode = new HuffmanTree(frequencyMapEncode);
                String encodedMessage = huffmanTreeEncode.encode(inputMessage);
                gui.setEncodedText(encodedMessage);
                huffmanTreeWrapper.value = huffmanTreeEncode;
            });

            gui.getFrequencyButton().addActionListener((ActionEvent evt) -> {
                String inputMessage = gui.getPlainText();
                final Map<Character, Integer> frequencyMap = getFrequencyMap(inputMessage);
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
                    if (encodedMessage.length() == 0) {
                        gui.setOutputText("Please encode a message first.");
                    } else {
                        String decodedMessage = huffmanTreeWrapper.value.decode(encodedMessage);
                        if (isValidBinaryString(encodedMessage)) {
                            gui.setPlainText(decodedMessage);
                        } else {
                            gui.setOutputText("Decode Error: You have entered invalid characters");
                            gui.setPlainText("");
                        }
                    }
                } else {
                    gui.setPlainText("Please encode a message first.");
                }
            });

            gui.getSaveHuffmanButton().addActionListener((ActionEvent evt) -> {
                saveHuffmanTreeToFile();
            });

            gui.getLoadHuffmanButton().addActionListener((ActionEvent evt) -> {
                loadHuffmanTreeFromFile();
            });
        });
    }

    public static void saveHuffmanTreeToFile() {
        String fileName = "C:\\Users\\yasit\\Desktop\\Huffman-Coding-GUI-using-netbeans\\project1\\a\\tree.txt"; // Adjust the file path as needed********
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

    public static void loadHuffmanTreeFromFile() {
        String fileName = "C:\\Users\\yasit\\Desktop\\Huffman-Coding-GUI-using-netbeans\\project1\\a\\tree.txt"; // Adjust the file path as needed*
        try {
        Map<Character, Integer> frequencyMap = readFrequencyMapFromFile(fileName);
        HuffmanTree huffmanTree = new HuffmanTree(frequencyMap);
        huffmanTreeWrapper.value = huffmanTree;

        StringBuilder output = new StringBuilder("Huffman tree loaded successfully.\n");
        output.append("Frequency Map:\n");
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            output.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        gui.setOutputText(output.toString());
    } catch (IOException e) {
        gui.setOutputText("Error loading Huffman tree from file: " + fileName + " (" + e.getMessage() + ")");
        e.printStackTrace();
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

    public static boolean isValidBinaryString(String str) {
        for (char c : str.toCharArray()) {
            if (c != '0' && c != '1') {
                return false;
            }
        }
        return true;
    }

    public static Map<Character, Integer> readFrequencyMapFromFile(String fileName) throws IOException {
    Map<Character, Integer> frequencyMap = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            if (parts.length == 3) { // Ensure that the line has three parts
                char character = parts[1].equals("none") ? '\0' : parts[1].charAt(0);
                if (Character.isLetter(character)) { // Check if it's an actual character
                    int frequency = Integer.parseInt(parts[2]);
                    frequencyMap.put(character, frequency);
                }
            }
        }
    }
    return frequencyMap;
}
}
