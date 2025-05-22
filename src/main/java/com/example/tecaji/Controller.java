package com.example.tecaji;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Controller {
    @FXML
    private Label labelText;
    @FXML
    private Label calcLabel;
    @FXML
    private CheckComboBox dropDown;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private CheckComboBox dropDown2;
    @FXML
    private DatePicker startDate2;
    @FXML
    private DatePicker endDate2;
    @FXML
    private TableView<Map<String, String>> tableId;
    @FXML
    private LineChart<Number, Number> lineId;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    //shranjeni podatki
    List<String> datums = new ArrayList<>();
    Map<String, String> currencies = new HashMap<>();
    Map<String, Map<String, CurrencyRate>> currencyData = new HashMap<>();
    ObservableList<Map<String, String>> tableData = FXCollections.observableArrayList();

    //funkcija, ki se zažene ob zagonu, ki polni podatke za dropdowne in datume, ter dobi podatke iz xmla
    @FXML
    public void initialize() throws Exception {

        xAxis.setLabel("Datum");
        yAxis.setLabel("Vrednost");

        lineId.setVisible(false);
        tableId.setVisible(false);

        String url = "http://www.bsi.si/_data/tecajnice/dtecbs-l.xml";
        String xmlData = GetXMLData.fetchXMLData(url);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputStream inputStream = new ByteArrayInputStream(xmlData.getBytes("UTF-8"));
        Document doc = builder.parse(inputStream);

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("tecajnica");

        //shrani podatke iz xmlaja
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element tecajnica = (Element) node;

                String datum = tecajnica.getAttribute("datum");
                datums.add(datum);
                Map<String, CurrencyRate> currencyRates = new HashMap<>();

                NodeList tecajList = tecajnica.getElementsByTagName("tecaj");

                for (int j = 0; j < tecajList.getLength(); j++) {
                    Node tecajNode = tecajList.item(j);

                    if (tecajNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element tecaj = (Element) tecajNode;

                        String oznaka = tecaj.getAttribute("oznaka");
                        String sifra = tecaj.getAttribute("sifra");
                        String value = tecaj.getTextContent();

                        CurrencyRate currencyRate = new CurrencyRate(oznaka, sifra, value);
                        currencyRates.put(oznaka, currencyRate);
                        currencies.put(sifra, oznaka);

                    }
                }

                currencyData.put(datum, currencyRates);

            }
        }

        //napolni podatke za dropdown iz limitira drugega na največ dve izbrani valuti
        dropDown.getItems().addAll(currencies.values());
        dropDown2.getItems().addAll(currencies.values());
        dropDown2.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                ObservableList<String> selectedItems = dropDown2.getCheckModel().getCheckedItems();

                if (selectedItems.size() > 2) {
                    String itemToRemove = selectedItems.get(selectedItems.size() - 1);
                    dropDown2.getCheckModel().clearCheck(itemToRemove);

                }
            }
        });

        //nastavitev limit za datume
        if (!datums.isEmpty()) {
            String firstDatum = datums.get(0);
            String lastDatum = datums.get(datums.size() - 1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate firstAllowedDate = LocalDate.parse(firstDatum, formatter);
            LocalDate lastAllowedDate = LocalDate.parse(lastDatum, formatter);

            startDate.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isBefore(firstAllowedDate) || date.isAfter(lastAllowedDate)) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            });
            startDate2.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isBefore(firstAllowedDate) || date.isAfter(lastAllowedDate)) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            });

            endDate.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isAfter(lastAllowedDate) || date.isBefore(firstAllowedDate)) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            });
            endDate2.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (date.isAfter(lastAllowedDate) || date.isBefore(firstAllowedDate)) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;");
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            });

            startDate.setValue(firstAllowedDate);
            endDate.setValue(lastAllowedDate);
            startDate2.setValue(firstAllowedDate);
            endDate2.setValue(lastAllowedDate);
        }
    }

    //funkcija, ki napolni tabelo in izriše graf
    @FXML
    protected void getData() {
        LocalDate selectedStartDate = startDate.getValue();
        LocalDate selectedEndDate = endDate.getValue();
        List<String> selectedCurrencies = dropDown.getCheckModel().getCheckedItems();
        if (selectedStartDate != null && selectedEndDate != null && !selectedCurrencies.isEmpty()) {
            Map<String, Map<String, CurrencyRate>> filteredData = getFilteredData(selectedStartDate, selectedEndDate, selectedCurrencies);

            tableData.clear();

            createDynamicColumns(selectedCurrencies);
            List<String> sortedDatums = new ArrayList<>(filteredData.keySet());
            Collections.sort(sortedDatums);

            for (String date : sortedDatums) {
                Map<String, CurrencyRate> rates = filteredData.get(date);
                Map<String, String> row = new HashMap<>();
                row.put("Datum", date);

                for (String currency : selectedCurrencies) {
                    String rate = rates.containsKey(currency) ? rates.get(currency).getValue() : "/";
                    row.put(currency, rate);
                }

                tableData.add(row);
            }

            tableId.setItems(tableData);
            tableId.setVisible(true);
            adjustTableWidth(selectedCurrencies.size());
            lineId.setVisible(true);

            updateLineChart(filteredData, selectedCurrencies, sortedDatums);
        } else {
            labelText.setText("Izberi vse vrednosti.");
        }
    }

    //filtriranje podatkov glede na ibrane vrednosti
    private Map<String, Map<String, CurrencyRate>> getFilteredData(LocalDate startDate, LocalDate endDate, List<String> selectedCurrencies) {
        Map<String, Map<String, CurrencyRate>> filteredData = new HashMap<>();

        for (Map.Entry<String, Map<String, CurrencyRate>> entry : currencyData.entrySet()) {
            String datum = entry.getKey();
            LocalDate datumDate = LocalDate.parse(datum, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            if ((datumDate.isEqual(startDate) || datumDate.isAfter(startDate)) &&
                    (datumDate.isEqual(endDate) || datumDate.isBefore(endDate))) {

                Map<String, CurrencyRate> currencyRates = entry.getValue();
                Map<String, CurrencyRate> filteredCurrencies = new HashMap<>();

                for (String currencyCode : selectedCurrencies) {
                    if (currencyRates.containsKey(currencyCode)) {
                        filteredCurrencies.put(currencyCode, currencyRates.get(currencyCode));
                    }
                }

                if (!filteredCurrencies.isEmpty()) {
                    filteredData.put(datum, filteredCurrencies);
                }
            }
        }

        return filteredData;
    }

    //dinamično sestavljanje stolpcev tabele
    private void createDynamicColumns(List<String> selectedCurrencies) {
        tableId.getColumns().clear();

        TableColumn<Map<String, String>, String> dateColumn = new TableColumn<>("Datum");
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get("Datum")));
        tableId.getColumns().add(dateColumn);

        for (String currency : selectedCurrencies) {
            TableColumn<Map<String, String>, String> currencyColumn = new TableColumn<>(currency);
            currencyColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().get(currency)));
            tableId.getColumns().add(currencyColumn);
        }
    }

    // določanje širine stolpcev in tabele glede na število izbranih valut
    private void adjustTableWidth(int numColumns) {
        int initialWidthIncrement = 100;
        int maxColumns = 6;

        double tableWidth = 0;

        if (numColumns <= maxColumns) {
            tableWidth = numColumns * initialWidthIncrement;
        } else {
            tableWidth = maxColumns * initialWidthIncrement;
        }

        tableId.setPrefWidth(tableWidth + 120);

        for (TableColumn<Map<String, String>, ?> column : tableId.getColumns()) {
            if (numColumns <= maxColumns) {
                column.setPrefWidth(initialWidthIncrement);
            } else {
                column.setPrefWidth(100);
            }
        }
    }

    //sestavljanje grafa za njegov izris
    private void updateLineChart(Map<String, Map<String, CurrencyRate>> filteredData, List<String> selectedCurrencies, List<String> sortedDatums) {

        lineId.getData().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Long> unixTimestamps = new ArrayList<>();
        for (String date : sortedDatums) {
            LocalDate localDate = LocalDate.parse(date, formatter);
            long unixTimestamp = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            unixTimestamps.add(unixTimestamp);
        }

        xAxis.setLabel("Datum");
        xAxis.setTickUnit(86400000);
        xAxis.setLowerBound(unixTimestamps.get(0));
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(unixTimestamps.get(unixTimestamps.size() - 1));
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickCount(0);

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(Number object) {
                long unixTimestamp = object.longValue();
                LocalDate date = Instant.ofEpochMilli(unixTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
                return date.format(dateFormatter);
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });

        for (String currency : selectedCurrencies) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(currency);

            for (String date : sortedDatums) {
                Map<String, CurrencyRate> rates = filteredData.get(date);

                CurrencyRate currencyRate = rates.get(currency);
                String rateStr = currencyRate.getValue();

                double rate = Double.parseDouble(rateStr);
                LocalDate currentDate = LocalDate.parse(date, formatter);

                long unixTimestamp = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

                XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(unixTimestamp, rate);

                series.getData().add(dataPoint);
                Platform.runLater(() -> {
                    javafx.scene.Node dataPointNode = dataPoint.getNode();
                    if (dataPointNode != null) {
                        Tooltip tooltip = new Tooltip(currency + "\nDatum: " + date + "\nTečaj: " + rate);
                        dataPointNode.setOnMouseEntered(event -> {
                            Tooltip.install(dataPointNode, tooltip);
                            tooltip.show(dataPointNode, event.getScreenX() + 10, event.getScreenY() + 10);
                        });
                        dataPointNode.setOnMouseExited(event -> {
                            tooltip.hide();
                        });
                    }
                });


            }

            lineId.getData().add(series);
        }
        lineId.setTitle("Tečaji");
        lineId.setVisible(true);
    }

    //izračunavanje podatkov za spodnji del aplikacije
    @FXML
    protected void getData2() {
        LocalDate selectedStartDate = startDate2.getValue();
        LocalDate selectedEndDate = endDate2.getValue();
        List<String> selectedCurrencies = dropDown2.getCheckModel().getCheckedItems();
        if (selectedStartDate != null && selectedEndDate != null && selectedCurrencies.size() > 1) {
            Map<String, Map<String, CurrencyRate>> filteredData = getFilteredData(selectedStartDate, selectedEndDate, selectedCurrencies);

            List<String> sortedDatums = new ArrayList<>(filteredData.keySet());
            Collections.sort(sortedDatums);

            String firstCurrencyFV = "";
            String firstCurrencySV = "";
            String secondCurrencyFV = "";
            String secondCurrencySV = "";
            String fCur = "";
            String sCur = "";

            for (int i = 0; i < sortedDatums.size(); i++) {
                String date = sortedDatums.get(i);
                if (i == 0 || i == sortedDatums.size() - 1) {
                    Map<String, CurrencyRate> rates = filteredData.get(date);
                    Map<String, String> row = new HashMap<>();
                    for (int j = 0; j < selectedCurrencies.size(); j++) {
                        String currency = selectedCurrencies.get(j);
                        String rate = rates.containsKey(currency) ? rates.get(currency).getValue() : "/";
                        if (i == 0 && j == 0) {
                            firstCurrencyFV = rate;
                            fCur = currency;
                        } else if (i == sortedDatums.size() - 1 && j == 0) {
                            firstCurrencySV = rate;
                        } else if (i == 0 && j == 1) {
                            secondCurrencyFV = rate;
                            sCur = currency;
                        } else if (i == sortedDatums.size() - 1 && j == 1) {
                            secondCurrencySV = rate;
                        }
                    }
                }

            }
            if (!firstCurrencyFV.equals("/") && !firstCurrencySV.equals("/") && !secondCurrencyFV.equals("/") && !secondCurrencySV.equals("/")) {
                Double firstCurrencyFVD = Double.parseDouble(firstCurrencyFV);
                Double firstCurrencySVD = Double.parseDouble(firstCurrencySV);
                Double secondCurrencyFVD = Double.parseDouble(secondCurrencyFV);
                Double secondCurrencySVD = Double.parseDouble(secondCurrencySV);

                Double calcFS = 1 * firstCurrencyFVD;
                Double calcFE = calcFS / firstCurrencySVD;
                calcFE = calcFE * 100;
                calcFE = (double) Math.round(calcFE);
                calcFE = calcFE / 100;

                Double calcSS = 1 * secondCurrencyFVD;
                Double calcSE = calcSS / secondCurrencySVD;
                calcSE = calcSE * 100;
                calcSE = (double) Math.round(calcSE);
                calcSE = calcSE / 100;

                if (calcFE > calcSE) {
                    if (calcFE > 1) {
                        calcLabel.setText("Če bi ob začetnem datumu vložili 1€ v prvo valuto " + fCur + " bi ob končnem datumu nazaj dobili " +
                                calcFE + "€, kar je " + round(calcFE - 1) + "€ več kot smo vložili vanjo.\nČe bi 1€ vložili v drugo valuto " +
                                sCur + " bi nazaj dobili " + calcSE + "€, kar je " + round(calcFE - calcSE) + "€ manj kot pri prvi.");
                    } else {
                        calcLabel.setText("Če bi ob začetnem datumu vložili 1€ v pvo valuto " + fCur + " bi ob končnem datumu nazaj dobili " + calcFE +
                                "€, kar je " + round(1 - calcFE) + "€ manj kot smo vložili vanjo.\nČe bi 1€ vložili v drugo valuto " + sCur +
                                " bi nazaj dobili " + calcSE + "€, kar je " + round(calcFE - calcSE) + "€ manj kot pri prvi.");
                    }
                } else {
                    if (calcSE > 1) {
                        calcLabel.setText("Če bi ob začetnem datumu vložili 1€ v drugo valuto " + sCur + " bi ob končnem datumu nazaj dobili " +
                                calcSE + "€, kar je " + round(calcSE - 1) + "€ več kot smo vložili vanjo.\nČe bi 1€ vložili v prvo valuto " +
                                fCur + " bi nazaj dobili " + calcFE + "€, kar je " + round(calcSE - calcFE) + "€ manj kot pri drugi.");
                    } else {
                        calcLabel.setText("Če bi ob začetnem datumu vložili 1€ v drugo valuto " + sCur + " bi ob končnem datumu nazaj dobili " + calcSE +
                                "€, kar je " + round(1 - calcSE) + "€ manj kot smo vložili vanjo.\nČe bi 1€ vložili v prvo valuto " + fCur +
                                " bi nazaj dobili " + calcFE + "€, kar je " + round(calcSE - calcFE) + "€ manj kot pri drugi.");
                    }
                }
            } else {
                calcLabel.setText("Tečaj za izbrano valuto v tem obdobju ne obstaja");
            }

        } else {
            calcLabel.setText("Please select a valid date range.");
        }
    }

    public static double round(double value) {
        double calc = value * 100;
        calc = (double) Math.round(calc);
        calc = calc / 100;
        return calc;
    }
}