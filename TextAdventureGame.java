package org;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;
import javax.sound.sampled.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class TextAdventureGame extends JFrame {
    private JLabel playerInfoLabel;
    private JTextArea sceneDescriptionArea;
    private JScrollPane sceneDescriptionScrollPane;
    private JPanel buttonPanel;
    private String playerName;
    private int gold;
    private int currentHP;
    private int maxHP;
    private List<String> inventory;
    private int foragingExperience;
    private int foragingLevel;
    private int foragingLevelRequirement;
    private int miningLevel;
    private int woodCuttingLevel;
    private JTable inventoryTable;
    private JFrame inventoryWindow;
    private Dimension defaultWindowSize = new Dimension(800, 600);
    private List<Area> areas; // List to store all areas in the game
    private Area currentArea; // Store the current area the player is in
    private boolean isForaging = false; // Tracks the foraging state
    private JLabel foragingSkillLabel;
    private JLabel notificationLabel;
    private JTextArea notificationTextArea;
    private JScrollPane notificationScrollPane;

    private class Area {
        private String name;
        private String description;
        private boolean canForageInArea;
        private List<String> forageableItems; // List of items that can be foraged in a given area...
        private List<Area> moveTo; // List to store areas that can be moved to from this area

        public Area(String name, String description, boolean canForageInArea, List<String> forageableItems) {
            this.name = name;
            this.description = description;
            this.canForageInArea = canForageInArea;
            this.forageableItems = forageableItems;
            this.moveTo = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean canForageInArea() {
            return canForageInArea;
        }

        public List<Area> getMoveTo() {
            return moveTo;
        }
        public List<String> getForageableItems() {
            return forageableItems;
        }
    } // Area

    private class ForageResultHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Random random = new Random();

            // Generate a random foraged item from the list of forageable items in the area you're in
            List<String> forageableItems = currentArea.getForageableItems();
            String foragedItem = forageableItems.get(random.nextInt(forageableItems.size()));

            // Check if inventory is full or almost full
            if (inventory.size() >= 35) {
                displayNotification("Your inventory is too full to carry any more items.");
            }
            else if (inventory.size() == 34) {
                inventory.add(foragedItem);
                displayNotification("You successfully foraged " + foragedItem + ". Your inventory is now full. Consider visiting a bank.");
            }
            else if (inventory.size() == 33) {
                inventory.add(foragedItem);
                displayNotification("You successfully foraged " + foragedItem + ". Your inventory is about to be full. Consider visiting a bank.");
            }
            else {
                inventory.add(foragedItem);
                displayNotification("You successfully foraged " + foragedItem + ".");
            }


            // Check for level up
            foragingExperience += 20;
            if (foragingExperience >= foragingLevelRequirement) {
                foragingExperience = 0;
                foragingLevel ++;
                foragingLevelRequirement += 50 + foragingLevelRequirement * 2;


                // Update the foraging skill level label in the inventory window
                updateForagingSkillLevel();
                displayNotification("You have leveled up your foraging skill! Your foraging skill is now level " + foragingLevel + "!");
            }

            // Update the window after foraging

            isForaging = false;

            // Re-enable the Forage button
            JButton forageButton = getForageButton();
            forageButton.setVisible(true);
            updateInventoryWindow();
        }
    }

    private void playForagingSound() {
        // Load the sound file
        String soundFilePath = "foragingSound.wav";
        Clip foragingClip = loadSound(soundFilePath);

        if(foragingClip != null) {
            foragingClip.start();
        }
    }

    public TextAdventureGame(String playerName) {
        this.playerName = playerName;
        this.gold = 0;
        this.currentHP = 10;
        this.maxHP = 10;
        this.inventory = new ArrayList<>();
        this.foragingExperience = 0;
        this.foragingLevelRequirement = 32;
        this.foragingLevel = 1;
        this.miningLevel = 1;
        this.woodCuttingLevel = 1;

        setTitle("Text Adventure Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Add areas to the game
        Area forestClearing1 = new Area("Forest Clearing 1", "Amidst the towering trees, you find yourself standing in a serene forest clearing. " +
                "Sunlight filters through the verdant canopy, casting dappled shadows on the lush undergrowth below. The air carries the earthy scent of moss and leaves, while a gentle " +
                "breeze rustles the leaves overhead. The ground beneath your feet is a carpet of soft grass and scattered wildflowers, and the distant murmur of a nearby brook adds a soothing " +
                "melody to the tranquil symphony of nature around you.", true,

                Arrays.asList("Wild Blueberry","Blackberry","Raspberry","Strawberry","Wild Apple","Chanterelle Mushroom","Chestnut","Hazelnut"));

        Area forestClearing2 = new Area("Forest Clearing 2", "The trees seem to stand closer together, their branches intertwining to create a natural canopy that filters " +
                "the sunlight into an enchanting, dappled play of light and shadow. The ground is covered in a soft layer of fallen leaves, their crunching sound underfoot punctuating the " +
                "gentle rustling of leaves. The air carries a slightly cooler breeze, infused with the scent of damp earth and the distant murmur of the nearby river. As you venture deeper, " +
                "the atmosphere grows even more mysterious, hinting at secrets hidden within the heart of this woodland realm.", true,

                Arrays.asList("Pine Nut","Acorn","Wild Grape","Elderberry","Wild Plum","Wild Apple","Chanterelle Mushroom","Chestnut","Nettle Leaves","Dandelion Greens"));

        Area riverBed = new Area("Riverbed","You find yourself standing along the riverbed, a tranquil oasis within the heart of the forest. The clear, glistening water flows " +
                "gently over smooth stones, its soothing murmur creating a harmonious symphony with the rustling leaves above. Towering trees cast their reflections upon the water's surface, " +
                "their branches swaying in the breeze. Wildflowers of vibrant hues adorn the banks, lending a splash of color to the serene landscape. As you take in the sights and sounds, a " +
                "sense of calmness envelops you, inviting you to linger and embrace the natural beauty that surrounds this hidden gem within the woods.",true,

                Arrays.asList("Smooth River Rock","Watermint Leaf","Freshwater Clam","Cattail Stalk","Bulrush","Wild Watercress","Riverbed Pebble","Strawberry","Raspberry"));

        Area caveEntrance = new Area("Cave Entrance","Before you stands a dark and mysterious cave entrance, a foreboding threshold leading into the depths of the earth. The air grows cooler " +
                "as you approach, carrying with it an earthy scent mixed with an enigmatic whisper that seems to beckon you forward. The entrance is framed by ancient, weathered stones, " +
                "bearing witness to the secrets concealed within. Shadows dance ominously within the depths, concealing untold mysteries and treasures yet to be discovered. As you peer into " +
                "the darkness, the unknown awaits, tempting you to step beyond the threshold and unravel the secrets that lie hidden within the heart of the cavern's embrace.",false,

                null);

        // Set the move destination for each area
        forestClearing1.getMoveTo().add(forestClearing2);
        forestClearing2.getMoveTo().add(forestClearing1);
        forestClearing2.getMoveTo().add(riverBed);
        forestClearing2.getMoveTo().add(caveEntrance);
        riverBed.getMoveTo().add(forestClearing2);
        caveEntrance.getMoveTo().add(forestClearing2);

        // Set starting area
        currentArea = forestClearing1;

        // Player Information Panel
        playerInfoLabel = new JLabel();
        playerInfoLabel.setForeground(Color.WHITE);
        playerInfoLabel.setFont(new Font("Arial", Font.PLAIN, 20));

        updatePlayerInfoLabel();

        // Create a panel for player information and center-align it
        JPanel playerInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        playerInfoPanel.setBackground(Color.BLACK);
        playerInfoPanel.add(playerInfoLabel);

        // Add the player information panel to the content pane
        add(playerInfoPanel, BorderLayout.NORTH);

        // Scene Description Panel W/ Scrollbar and Center Alignment
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.BLACK);

        sceneDescriptionArea = new JTextArea(10, 30);
        sceneDescriptionArea.setBackground(Color.BLACK);
        sceneDescriptionArea.setForeground(Color.WHITE);
        sceneDescriptionArea.setFont(new Font("Arial", Font.PLAIN, 20));
        sceneDescriptionArea.setEditable(false);
        sceneDescriptionArea.setLineWrap(true);
        sceneDescriptionArea.setWrapStyleWord(true);



        sceneDescriptionScrollPane = new JScrollPane(sceneDescriptionArea);
        sceneDescriptionScrollPane.getViewport().setBackground(Color.BLACK);

        // Create the Inventory button
        JButton inventoryButton = new JButton("Inventory");
        inventoryButton.setForeground(Color.WHITE);
        inventoryButton.setBackground(Color.BLACK);
        inventoryButton.setFont(new Font("Arial", Font.PLAIN, 18));
        inventoryButton.setFocusPainted(false);
        inventoryButton.setToolTipText("View your inventory.");

        // Create the notification text area
        notificationTextArea = new JTextArea(3, 30);
        notificationTextArea.setBackground(Color.BLACK);
        notificationTextArea.setForeground(Color.YELLOW);
        notificationTextArea.setFont(new Font("Arial",Font.BOLD, 18));
        notificationTextArea.setEditable(false);
        notificationTextArea.setLineWrap(true);
        notificationTextArea.setWrapStyleWord(true);
        notificationTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        notificationScrollPane = new JScrollPane(notificationTextArea);
        notificationScrollPane.getViewport().setBackground(Color.BLACK);
        notificationScrollPane.setBorder(BorderFactory.createLineBorder(Color.YELLOW,2));

        // Create a main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        // Create a nested panel for the player info and scene description
        JPanel playerScenePanel = new JPanel(new BorderLayout());
        playerScenePanel.setBackground(Color.BLACK);

        // Add player info label to the playerScenePanel
        playerScenePanel.add(playerInfoPanel, BorderLayout.NORTH);

        // Add scene description scroll pane to the playerScenePanel
        playerScenePanel.add(sceneDescriptionScrollPane, BorderLayout.CENTER);

        // Add the notification scroll pane to the playerScenePanel
        playerScenePanel.add(notificationScrollPane, BorderLayout.SOUTH);

        // Add the playerScenePanel to the main panel
        mainPanel.add(playerScenePanel, BorderLayout.CENTER);

        // Create a panel for inventory button with FlowLayout
        JPanel inventoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inventoryButtonPanel.setBackground(Color.BLACK);
        inventoryButtonPanel.add(inventoryButton);

        // Add the inventory button panel to the main panel's SOUTH
        mainPanel.add(inventoryButtonPanel, BorderLayout.SOUTH);

        // Add the main panel to the content pane


        // Add action listener to the Inventory button
        inventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inventoryWindow == null) {
                    showInventoryWindow();
                } else {
                    inventoryWindow.dispose();
                    inventoryWindow = null;
                    // Remove setSize(defaultWindowSize);
                    requestFocusForGameWindow();
                }
            }
        });



        add(mainPanel, BorderLayout.CENTER);

        // Button Panel
        buttonPanel = new JPanel(new GridLayout(1, 0, 10, 10));
        buttonPanel.setBackground(Color.BLACK);
        add(buttonPanel, BorderLayout.SOUTH);




        // Set initial scene
        setScene(currentArea.getDescription());

        // Add key listener to the main window
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if(keyCode == KeyEvent.VK_I && !isForaging) {
                    showInventoryWindow();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });

        // Add KeyListener to the scene description area
        sceneDescriptionArea.setFocusable(true);

        sceneDescriptionArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if(keyCode == KeyEvent.VK_UP) {
                    JScrollBar verticalScrollBar = sceneDescriptionScrollPane.getVerticalScrollBar();
                    verticalScrollBar.setValue(verticalScrollBar.getValue() - verticalScrollBar.getBlockIncrement());
                }
                else if(keyCode == KeyEvent.VK_DOWN) {
                    JScrollBar verticalScrollBar = sceneDescriptionScrollPane.getVerticalScrollBar();
                    verticalScrollBar.setValue(verticalScrollBar.getValue() + verticalScrollBar.getBlockIncrement());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

        });




        pack();
        setSize(defaultWindowSize); // Set the default window size
        setLocationRelativeTo(null);
        setVisible(true);


    } // End of the TextGame Constructor

    private void showDestinationOptionsPopup(List<Area> destinations) {
        JDialog dialog = new JDialog(this, "Choose Destination", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setBackground(Color.BLACK);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBackground(Color.BLACK);

        for (Area destination : destinations) {
            JButton destinationButton = new JButton(destination.getName());
            destinationButton.setForeground(Color.WHITE);
            destinationButton.setBackground(Color.BLACK);
            destinationButton.setFont(new Font("Arial", Font.PLAIN, 18));
            destinationButton.setFocusPainted(false);

            destinationButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveToArea(destination);
                    dialog.dispose();
                }
            });
            buttonPanel.add(destinationButton);
        }
        dialog.add(buttonPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private void displayNotification(String message) {
        notificationTextArea.setText(message);
    }
    private void move() {
        // Check if there are any destinations to move to
        List<Area> moveToAreas = currentArea.getMoveTo();
        if(moveToAreas.isEmpty()) {
            setScene("There is nowhere to move to from this area.");
            return;
        }

        showDestinationOptionsPopup(moveToAreas);
    }
    private void moveToArea(Area destination) {
        currentArea = destination;
        setScene(currentArea.getDescription());
    }
    private Clip loadSound(String filename) {
        try {
            // Use the class loader to load the sound file as a resource
            ClassLoader classLoader = TextAdventureGame.class.getClassLoader();
            URL resourceURL = classLoader.getResource(filename);

            System.out.println("Resource URL: " + resourceURL);

            if (resourceURL == null) {
                System.out.println("Sound file was not found: " + filename);
                throw new FileNotFoundException("Sound file not found: " + filename);
            }



            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resourceURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void showInventoryWindow() {
        // Create the inventory window only if it's null
        inventoryWindow = new JFrame("Inventory");
        inventoryWindow.requestFocusInWindow();
        inventoryWindow.setPreferredSize(defaultWindowSize);
        inventoryWindow.setLayout(new BorderLayout());
        inventoryWindow.getContentPane().setBackground(Color.BLACK);
        inventoryWindow.setFocusable(true);

        // Create two panels for the left and right sides of the inventory window
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.BLACK);
        rightPanel.setBackground(Color.BLACK);

        // Create the inventory table for the left panel
        String[] columnNames = {"Items"};
        Object[][] data = new Object[inventory.size()][1];
        for(int i = 0; i < inventory.size(); i++) {
            data[i][0] = inventory.get(i);
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class; // Set the column class to String to avoid the default renderer
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setBackground(Color.BLACK);
        inventoryTable.setForeground(Color.WHITE);
        inventoryTable.setFont(new Font("Arial", Font.PLAIN, 22));
        inventoryTable.setEnabled(false);

        // Use a custom cell renderer to center the item text vertically and add a margin
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value != null) {
                    // Set the text and add margin for vertical space
                    setText(" " + value.toString() + " ");
                }
                else {
                    setText("");
                }
            }
        };
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setVerticalAlignment(JLabel.CENTER);

        // Set a larger margin for vertical space between item names and cell borders
        int topPadding = 8;
        int leftPadding = 20;
        int bottomPadding = 8;
        int rightPadding = 20;
        Border padding = BorderFactory.createEmptyBorder(topPadding, leftPadding, bottomPadding, rightPadding);
        centerRenderer.setBorder(BorderFactory.createCompoundBorder(centerRenderer.getBorder(), padding));

        // Calculate the optimal font size based on the cell height
        Font defaultFont = new Font("Arial", Font.PLAIN, 22);
        FontMetrics fontMetrics = getFontMetrics(defaultFont);
        int lineHeight = fontMetrics.getHeight();
        int cellHeight = inventoryTable.getRowHeight();
        float maxFontSize = defaultFont.getSize2D();

        while (lineHeight > cellHeight - topPadding - bottomPadding && maxFontSize > 10) {
            maxFontSize -= 1;
            centerRenderer.setFont(defaultFont.deriveFont(maxFontSize));
            fontMetrics = getFontMetrics(centerRenderer.getFont());
            lineHeight = fontMetrics.getHeight();
        }

        inventoryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        inventoryTable.setRowHeight(Math.max(cellHeight, lineHeight + topPadding + bottomPadding)); // Adjust row height

        JScrollPane inventoryScrollPane = new JScrollPane(inventoryTable);
        inventoryScrollPane.getViewport().setBackground(Color.BLACK);
        leftPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        // Create the skill levels panel for the right panel
        JPanel skillPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        skillPanel.setBackground(Color.BLACK);
        rightPanel.add(skillPanel, BorderLayout.NORTH);

        // Add the skill levels to the skill panel
        String[] skills = {"Foraging","Mining","Wood Cutting"}; // Add more skills as needed
        int[] skillLevels = {foragingLevel, miningLevel, woodCuttingLevel};
        for(int i = 0; i < skills.length; i++) {
            JLabel skillLabel = new JLabel(skills[i] + " Level - " + skillLevels[i]);
            skillLabel.setForeground(Color.WHITE);
            skillLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            skillPanel.add(skillLabel);
            if(skills[i].equals("Foraging")) {
                foragingSkillLabel = skillLabel;
            }
        }

        // Add the left and right panels to the inventory window
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400); // Adjust the divider location as needed
        splitPane.setResizeWeight(0.5);
        inventoryWindow.add(splitPane, BorderLayout.CENTER);

        inventoryWindow.pack();
        inventoryWindow.setLocationRelativeTo(this);


        inventoryWindow.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_I) {
                    inventoryWindow.dispose();
                    inventoryWindow = null;
                    setSize(defaultWindowSize);
                }
            }
        });

        // Add a WindowListener to handle window closing event
        inventoryWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Dispose the inventory window when it's closed
                inventoryWindow.dispose();
                inventoryWindow = null; // Set the inventoryWindow to null to indicate that it's closed
                setSize(defaultWindowSize);
                requestFocusInWindow();
            }
        });

        inventoryWindow.setVisible(true);

    }

    private void requestFocusForGameWindow() {
        requestFocus();
        sceneDescriptionArea.requestFocusInWindow();
    }

    private void forage() {
        if (!currentArea.canForageInArea) {
            displayNotification("You cannot forage in this area!");
            return;
        }

        // Remove the Forage button from the button panel while foraging
        JButton forageButton = getForageButton();
        forageButton.setVisible(false);

        isForaging = true;


        displayNotification("You begin to forage...");

        playForagingSound();

        // Create a Swing Timer with a 5-second delay
        int forageDelay = 5000; // 5 seconds
        Timer forageTimer = new Timer(forageDelay, new ForageResultHandler());


        // Start the timer
        forageTimer.setRepeats(false); // Execute the action only once
        forageTimer.start();
    }
    private void updateForagingSkillLevel() {
        if (inventoryWindow != null && foragingSkillLabel != null) {
            foragingSkillLabel.setText("Foraging Level - " + foragingLevel);
            inventoryWindow.repaint();
        }
    }
    private void updatePlayerInfoLabel() {
        playerInfoLabel.setText("Name: " + playerName + "    | Gold: " + gold + "    | HP: " + currentHP + "/" +
                maxHP);
    } // End of updatePlayerInfoLabel method
    private JButton getForageButton() {
        for (Component component : buttonPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().equals("Forage")) {
                    return button;
                }
            }
        }
        return null;
    }
    private void setScene(String sceneDescription) {
        sceneDescriptionArea.setText(sceneDescription);
        sceneDescriptionArea.setCaretPosition(0); // Scroll to the top?
        sceneDescriptionScrollPane.getVerticalScrollBar().setValue(0); // Reset the scrollbar position
        notificationTextArea.setText("");

        buttonPanel.removeAll();

        // Update the foraging skill level label in the inventory window
        updateForagingSkillLevel();

        addButton("Forage","You search for foood.", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        addButton("Examine Surroundings", "You look around...", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TextAdventureGame.this.setScene("You see tall trees and a small river nearby.");
            }
        });

        // Add a Move button
        addButton("Move","Click to move to another area.", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                move();
            }
        });



        // Add more actions based on the scene as needed...
        buttonPanel.revalidate();
        buttonPanel.repaint();
        revalidate();
        repaint();
    } // End of the setScene method
    private void addButton(String label, String result, ActionListener actionListener) {
        JButton button = new JButton(label);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setFocusPainted(false);
        buttonPanel.add(button);

        if(label.equals("Forage")) {
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isForaging) {
                        forage(); // Call the forage method when the 'Forage' button is clicked
                    }

                }
            });
        }
        else {
            // For other buttons, set the provided action listener
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isForaging) {
                        actionListener.actionPerformed(e);
                    }

                }
            });
        }
        button.setToolTipText(result);
    }
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            String playerName = "Matthew Pendleton";
            new TextAdventureGame(playerName);
        });
    }
    public void updateInventoryWindow() {
        if (inventoryWindow == null) {
            // If the inventory window is not yet created, return without doing anything
            return;
        }

        // Get the inventory table model and update the data
        DefaultTableModel tableModel = (DefaultTableModel) inventoryTable.getModel();
        tableModel.setRowCount(inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            tableModel.setValueAt(inventory.get(i), i, 0);
        }


        updateForagingSkillLevel();
        // Repaint the inventory window to update the display
        inventoryWindow.repaint();
        requestFocusForGameWindow();
    }

}