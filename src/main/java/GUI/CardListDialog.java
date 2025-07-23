/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import Controllers.CardController;
import Controllers.UserController;
import Domain.Card;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *
 * @author sovi8
 */
public class CardListDialog extends JDialog {
    private final Map<Integer, Card> cardMap;
    private JTable cardTable;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> editionComboBox;
    private JLabel totalCards;
    
    public boolean showCardCount;

    public CardListDialog(Window parent, Map<Integer, Card> cardMap, String title, boolean showCardCount) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        
        this.cardMap = new HashMap<>(cardMap); // Copia el mapa para evitar interferencias externas
        this.showCardCount = showCardCount;
        configureWindow(parent);
        addSearchAndFilterPanel();
        configureTable(cardMap, showCardCount);  // Llama a configureTable con cardMap
        loadData(cardMap);        // Cargar datos en la tabla y actualizar el comboBox de ediciones
        addEditionComboBoxListener();     
        
    }

    protected void configureWindow(Window parent) {
        setLayout(new BorderLayout());
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    protected void configureTable(Map<Integer, Card> cardMap, boolean showCardCount) {
        model = CardTableBuilder.generateCardTableModel(cardMap, this, showCardCount);
        cardTable = new JTable(model);

        sorter = new TableRowSorter<>(model);
        cardTable.setRowSorter(sorter);
        ocultarColumnaID();

        JScrollPane scrollPane = new JScrollPane(cardTable);
        add(scrollPane, BorderLayout.CENTER);

        cardTable.addMouseListener(new CardTableMouseListener(cardMap, cardTable, getParentFrame()));
        CardTableBuilder.configureTableVisuals(cardTable);
    }
    
    // Método para actualizar el texto de la etiqueta totalCards
    protected void updateTotalCards(int count){
       totalCards.setText("Cartas totales: " + count);
    }

    // Método para llenar el modelo de la tabla con los datos de las cartas y actualizar el comboBox
    protected void loadData(Map<Integer, Card> cardMap) {
        model.setRowCount(0);

        String[] editions = CardController.getEditionName(cardMap).toArray(new String[0]);
        updateEditionComboBox(editions);

        CardTableBuilder.cardsToTable(model, new HashSet<>(cardMap.values()));
         if(cardMap == UserController.cardMap){
            int userCounter = 0;
            for(Card c : cardMap.values()){
                userCounter = userCounter + c.getCardCount();
            }
            updateTotalCards(userCounter);          
        } else{
             updateTotalCards(cardMap.size());
        }
    }
    
    public void refreshTable() {
        if (this.getTitle().equals("Mis Cartas")) { // Solo recarga si es "Mis Cartas"
            loadData(cardMap); // Recarga los datos en la tabla
            model.fireTableDataChanged(); // Notifica a la tabla que los datos han cambiado
        }
    }
    
    // Método para actualizar el JComboBox de ediciones
    protected void updateEditionComboBox(String[] editions) {
        if (editionComboBox == null) {
            System.err.println("Error: editionComboBox no ha sido inicializado.");
            return;
        }
        editionComboBox.setModel(new DefaultComboBoxModel<>(editions));
        editionComboBox.setSelectedItem("TODAS"); // Selecciona "TODAS" como predeterminado
    }

    // Método para ocultar la columna de ID (columna 0) en la tabla
    protected void ocultarColumnaID() {
        cardTable.getColumnModel().getColumn(0).setMinWidth(0);
        cardTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cardTable.getColumnModel().getColumn(0).setPreferredWidth(0);
    }

    // Panel de búsqueda y filtros, incluyendo el comboBox de edición y totalCards
    protected void addSearchAndFilterPanel() {
    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
    filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel searchLabel = new JLabel("Buscar Carta:");
    JTextField searchField = new JTextField(15);

    JLabel editionLabel = new JLabel("Edición:");
    editionComboBox = new JComboBox<>();

    JButton btnNuevoDeck = new JButton("Nuevo Deck");

    String[] decksUsuario = {"Deck 1", "Deck 2", "Deck 3"}; // sustituir por carga dinámica
    JComboBox<String> comboDecks = new JComboBox<>(decksUsuario);

    totalCards = new JLabel("Cartas totales: 0");
    totalCards.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    // listeners
    searchField.addActionListener(e -> filterTable(searchField.getText()));
    editionComboBox.addActionListener(e -> filterByEdition((String) editionComboBox.getSelectedItem()));
    btnNuevoDeck.addActionListener(e -> {
        // lógica de creación de deck
    });
    comboDecks.addActionListener(e -> {
        // lógica de carga de deck
    });

    // añadir elementos con separación fija
    filterPanel.add(searchLabel);
    filterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    filterPanel.add(searchField);
    filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    filterPanel.add(editionLabel);
    filterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    filterPanel.add(editionComboBox);
    filterPanel.add(Box.createRigidArea(new Dimension(15, 0)));
    filterPanel.add(btnNuevoDeck);
    filterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    filterPanel.add(comboDecks);
    filterPanel.add(Box.createHorizontalGlue());
    filterPanel.add(totalCards);

    add(filterPanel, BorderLayout.NORTH);
}


    protected void filterTable(String query) {
        sorter.setRowFilter(query.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + query, 1));
    }

    protected void addEditionComboBoxListener() {
        editionComboBox.addActionListener(e -> {
            String selectedEdition = (String) editionComboBox.getSelectedItem();
            filterByEdition(selectedEdition);
        });
    }

    protected void filterByEdition(String selectedEdition) {
        if (model == null) {
            return;
        }

        model.setRowCount(0); // Limpia el modelo antes de cargar los datos filtrados
        AtomicInteger count = new AtomicInteger(0);

        // Filtra el mapa local
        if ("TODAS".equals(selectedEdition)) {
            // Mostrar todas las cartas del mapa local
            cardMap.values().forEach(card -> {
                model.addRow(CardTableBuilder.getRowData(card));
                count.incrementAndGet();
            });
        } else {
            // Mostrar solo cartas de la edición seleccionada
            cardMap.values().forEach(card -> {
                if (card.getSet_name().equals(selectedEdition)) {
                    model.addRow(CardTableBuilder.getRowData(card));
                    count.incrementAndGet();
                }
            });
        }

        updateTotalCards(count.get()); // Actualiza el total de cartas visibles
    }

    // Obtener el JFrame desde el componente actual usando getParent()
    protected JFrame getParentFrame() {
        Component parent = this.getParent();
        while (!(parent instanceof JFrame) && parent != null) {
            parent = parent.getParent();
        }
        return (JFrame) parent;
    }
}