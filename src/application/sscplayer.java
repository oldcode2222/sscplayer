package application;

import java.io.File;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class sscplayer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            BorderPane root = new BorderPane();
            
            File f = new File("application/test.mp4");
            
            Media Video = new Media(f.toURI().toString());
            MediaPlayer Play = new MediaPlayer(Video);
            MediaView mediaView = new MediaView(Play);
            mediaView.setFitWidth(mediaView.getFitWidth());
            mediaView.setFitHeight(mediaView.getFitHeight());
            root.setCenter(mediaView);

            HBox bottomNode = new HBox(10.0);
            bottomNode.getChildren().add(createButton(Play));
            bottomNode.getChildren().add(createTimeSlider(Play));
            bottomNode.getChildren().add(createVolumeSlider(Play));
            root.setBottom(bottomNode);
            
            Scene scene = new Scene(root, 900, 600);
            
            primaryStage.setTitle("sscplayer");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            Play.play();
            Play.currentTimeProperty().addListener((ov) -> System.out.println(Play.getCurrentTime()));
            Play.statusProperty().addListener((ov) -> System.out.println(Play.getStatus()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public Node createButton(MediaPlayer mp) {
        HBox root = new HBox(1.0);
        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button stopButton = new Button("Stop");
        ToggleButton repeatButton = new ToggleButton("Repeat");
        root.getChildren().add(playButton);
        root.getChildren().add(pauseButton);
        root.getChildren().add(stopButton);
        root.getChildren().add(repeatButton);

        EventHandler<ActionEvent> playHandler = (e) ->
            {
                mp.play();
            };
        playButton.addEventHandler(ActionEvent.ACTION, playHandler);

        EventHandler<ActionEvent> pauseHandler = (e) ->
            {
                mp.pause();
            };
        pauseButton.addEventHandler(ActionEvent.ACTION, pauseHandler);

        EventHandler<ActionEvent> stopHandler = (e) ->
            {
                mp.stop();
            };
        stopButton.addEventHandler(ActionEvent.ACTION, stopHandler);

        Runnable repeatFunc = () ->
            {
                if(repeatButton.isSelected()) {
                    mp.seek(mp.getStartTime());
                    mp.play();
                } else {
                    mp.seek(mp.getStartTime());
                    mp.stop();
                };
            };
        mp.setOnEndOfMedia(repeatFunc);
        return root;
    }

    public Node createTimeSlider(MediaPlayer mp) {
        HBox root = new HBox(5.0);
        Slider slider = new Slider();
        Label info = new Label();
        root.getChildren().add(slider);
        root.getChildren().add(info);

        Runnable beforeFunc = mp.getOnReady();
        Runnable readyFunc = () ->
            {
                if(beforeFunc != null) {
                    beforeFunc.run();
                }

                slider.setMin(mp.getStartTime().toSeconds());
                slider.setMax(mp.getStopTime().toSeconds());
                slider.setSnapToTicks(true);
            };
        mp.setOnReady(readyFunc);

        ChangeListener<? super Duration> playListener = (ov, old, current) ->
            {
                System.out.println("here");
                String infoStr = String.format("%4.2f", mp.getCurrentTime().toSeconds()) + "/" + String.format("%4.2f", mp.getTotalDuration().toSeconds());
                info.setText(infoStr);
                slider.setValue(mp.getCurrentTime().toSeconds());
            };
        mp.currentTimeProperty().addListener(playListener);

        EventHandler<MouseEvent> sliderHandler = (e) ->
            {
                mp.seek(javafx.util.Duration.seconds(slider.getValue()));
            };
        slider.addEventFilter(MouseEvent.MOUSE_RELEASED, sliderHandler);
        return root;
    }

    public Node createVolumeSlider(MediaPlayer mp) {
        HBox root = new HBox(5.0);
        Label info = new Label();
        Slider slider = new Slider();
        root.getChildren().add(info);
        root.getChildren().add(slider);

        Runnable beforeFunc = mp.getOnReady();
        Runnable readyFunc = () ->
            {
                if(beforeFunc != null) {
                    beforeFunc.run();
                }

                slider.setMin(0.0);
                slider.setMax(1.0);
                slider.setValue(mp.getVolume());
            };
        mp.setOnReady(readyFunc);

        ChangeListener<? super Number> sliderListener = (ov, old, current) ->
            {
                String infoStr = String.format("Vol:%4.2f", mp.getVolume());
                info.setText(infoStr);
                mp.setVolume(slider.getValue());
            };
        slider.valueProperty().addListener(sliderListener);
        return root;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
