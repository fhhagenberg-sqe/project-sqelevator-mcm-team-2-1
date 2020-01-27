package at.fhhagenberg.sqelevator.gui;

import at.fhhagenberg.sqelevator.communication.ElevatorChangeListener;
import at.fhhagenberg.sqelevator.communication.UIActionListener;
import at.fhhagenberg.sqelevator.model.Elevator;
import at.fhhagenberg.sqelevator.model.ElevatorSystem;
import at.fhhagenberg.sqelevator.model.states.CommittedDirection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ElevatorController implements Initializable, ElevatorChangeListener {

    public static final int PREF_HEIGHT = 30;

    @FXML public Text elevatorName;
    @FXML public Text elevatorDirection;
    @FXML public Text speed;
    @FXML public Text acceleration;
    @FXML public Text weight;
    @FXML public Text doorStatus;
    @FXML public TextField manualInput;

    @FXML public Pane gridContainer;

    private ImageView elevatorImage = null;

    private UIActionListener uiActionListener;
    private int maxFloor;

    private Elevator elevator;

    public void setUiActionListener(UIActionListener uiActionListener) {
        this.uiActionListener = uiActionListener;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadImage(); //TODO: Move to some singleton
    }

    @FXML
    public void sendRequest() {
        String text = manualInput.getText();
        // text must be a positive integer and not be empty
        if(!text.isEmpty() && text.matches("^[1-9]\\d*$") && uiActionListener!=null) {
            Integer selectedFloor = Integer.parseInt(text)-1;
            if(selectedFloor<maxFloor) {
                uiActionListener.floorSelected(elevator.getId(), selectedFloor);
                if(selectedFloor<elevator.getFloor())
                    uiActionListener.changeCommittedDirection(elevator.getId(), CommittedDirection.DOWN);
                else if(selectedFloor>elevator.getFloor())
                    uiActionListener.changeCommittedDirection(elevator.getId(), CommittedDirection.UP);
            }
        }
    }

    @Override
    public void update(ElevatorSystem system) {
        //TODO: better threading!
        Platform.runLater(() -> {
            elevator = system.getElevators().get(0);
            if(elevator!=null) {
                maxFloor = system.getFloorCount();
                elevatorName.setText("Elevator " + elevator.getId());
                elevatorDirection.setText(elevator.getCommittedDirection().getPrintValue());
                speed.setText(elevator.getSpeed() + " ft/s");
                acceleration.setText("Max: " + elevator.getAcceleration() + "ft/s²");
                weight.setText(elevator.getWeight() + " kg");
                doorStatus.setText(elevator.getDoorStatus().getPrintValue());
                updateElevatorGrid(system); // TODO: Only redraw things that change
            }
        });

    }

    private void updateElevatorGrid(ElevatorSystem elevatorSystem) {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true); //TODO: Use css instead, as this is reserved for debugging purposes

        int currentFloor = elevator.getFloor();

        for(int i = elevatorSystem.getFloorCount()-1; i >=0; i--) {
            Text levelText = new Text();
            levelText.setText(String.valueOf(i+1) + "   " + elevatorSystem.getFloorButtons().get(i).getPrintValue());
            gridPane.add(levelText, 0, elevatorSystem.getFloorCount()-1-i);

            StackPane placeholder = new StackPane();
            placeholder.setPrefWidth(PREF_HEIGHT);
            placeholder.setPrefHeight(PREF_HEIGHT);

            if(i == currentFloor) {
                placeholder.getChildren().add(elevatorImage);
            }

            gridPane.add(placeholder, 1, elevatorSystem.getFloorCount()-1-i);
        }

        gridContainer.getChildren().clear();
        gridContainer.getChildren().add(gridPane);
    }

    private void loadImage() {
        try {
            Image image = new Image((getClass().getClassLoader().getResource("icons8-elevator-64.png").openStream()));
            elevatorImage = new ImageView(image);
            elevatorImage.setFitHeight(28);
            elevatorImage.setFitWidth(28);
        } catch (FileNotFoundException e) {
            System.err.println("Image not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
