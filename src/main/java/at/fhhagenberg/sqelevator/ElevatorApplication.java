package at.fhhagenberg.sqelevator;

import at.fhhagenberg.sqelevator.connection.RMIElevatorServiceFetcher;
import at.fhhagenberg.sqelevator.gui.DashboardController;
import at.fhhagenberg.sqelevator.statemanagement.ElevatorManagement;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqelevator.IElevator;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class ElevatorApplication extends Application {

    private static Logger logger = LoggerFactory.getLogger(ElevatorApplication.class);

    public IElevator getRMIInstance() {
        try {
            return RMIElevatorServiceFetcher.getElevatorService();
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            logger.error("Error fetching the rmi interface", e);
        }
        return null;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("dashboard.fxml"));
        IElevator rmiInstance = getRMIInstance();
        ElevatorManagement management = new ElevatorManagement(rmiInstance);
        Parent root = (Parent) fxmlLoader.load();
        DashboardController controller = fxmlLoader.<DashboardController>getController();
        management.addListener(controller);
        controller.setUiListener(management);

        primaryStage.setTitle("Elevator Control 2000");
        primaryStage.setScene(new Scene(root, 1600, 900));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
        // check this: https://stackoverflow.com/questions/52682195/how-to-get-javafx-and-java-11-working-in-intellij-idea
        // problem was that the debugging did not work when using the maven javafx:run job, even if I started the job to be debugged
        // thus, if you wish to debug the application, follow this guide
    }
}