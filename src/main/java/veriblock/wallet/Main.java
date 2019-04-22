// VeriBlock GUI Wallet
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import veriblock.wallet.core.FileManager;
import veriblock.wallet.core.Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import veriblock.wallet.core.locale.LocaleResourceAnalyzer;
import veriblock.wallet.features.shell.MainController;

import java.awt.*;


public class Main extends Application {
    private static final Logger _logger = LoggerFactory.getLogger(Main.class);

    private static final int MIN_WINDOW_WIDTH = 1400;
    private static final int MIN_WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Not Set Here...");


        FileManager fm = new FileManager();
        fm.getRootDirectory();

        //----

        //Pass in reference to primary stage to enable modal popups
        FXMLLoader loader = new FXMLLoader(getClass().getResource("features/shell/Main.fxml"));
        Parent root = loader.load(); //calls initialize()
        MainController mainController = loader.getController();
        mainController.setPrimaryStage(primaryStage);
        mainController.start();

        // This should not be set or the same size as scene. Else stage will out grow the scene hence leaving some white area

        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);

        try {
            //image put in root, next to properties: nodecore_wallet_ui.properties
            Image icon = new Image(Utils.resourceAsExternal("vbk_icon.png"));
            primaryStage.getIcons().add(icon);

            //Add stylesheets
            //This will find in sub-folder, nodecore-wallet-ui\bin\testFiles
            root.getStylesheets().clear();
            root.getStylesheets().add(Utils.resourceAsExternal("default.css"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        _logger.info("Found " + screens.length + " monitors:");
        for (int i = 0; i < screens.length; i++) {
            _logger.info("\tMonitor " + i + ":");
            _logger.info("\t" + screens[i].getDisplayMode().getWidth() + "x" + screens[i].getDisplayMode().getHeight());
        }

        DisplayMode mode = screens[0].getDisplayMode();

        int targetWidth = Math.min(MIN_WINDOW_WIDTH, (int)(mode.getWidth() * 0.85));
        int targetHeight = Math.min(MIN_WINDOW_HEIGHT, (int)(mode.getHeight() * 0.85));


        primaryStage.setScene(new Scene(root, targetWidth, targetHeight));
        //primaryStage.setX(0);
        //primaryStage.setY(5);
        primaryStage.show();

        /*
        if (primaryStage.getWidth() != targetWidth) {
            primaryStage.setWidth(primaryStage.getWidth() * 0.85);
            primaryStage.setHeight(primaryStage.getHeight() * 0.85);
        }
*/
        System.out.println("primaryStage.width=" + primaryStage.getWidth());
    }


    public static void main(String[] args) {

        boolean blnFoundCommands = checkForCommands(args);
        if (blnFoundCommands) {
            //Don't run GUI app, use this as console app instead
            System.out.println("Exit application, ran command instead of GUI app");
            System.exit(0);
            return;
        }

        launch(args);
    }

    //IF found commands, then run that, instead of showing the GUI app
    //NOTE: packaging this in with the wallet for ease of use, could eventually expose the analyzer as a wallet
    //feature that the public could use
    private static boolean checkForCommands(String[] args) {

        //args = new String[]{"locale", "merge", "test", "zh_cn", "..\\..\\..\\..\\src\\main\\resources\\locale"};

        //example:
        //locale compare test zh
        boolean blnFoundCommands = false;
        if (args.length == 0) {
            return blnFoundCommands;
        } else {
            blnFoundCommands = true;
        }

        //run app
        String command = args[0];

        System.out.println(String.format("Command: %1$s", command));

        try {
            if (command.equals("locale")) {
                System.out.println("Run locale compare");
                //run Locale Analyzer
                LocaleResourceAnalyzer lra = new LocaleResourceAnalyzer();
                String subCommand = args[1];    //compare | compare_merge
                String localePrimary = args[2];
                String localeSecondary = args[3];
                String resourceFolderOverride = null;
                if (args.length > 4) {
                    resourceFolderOverride = args[4];
                }

                System.out.println("About to run LocaleResourceAnalyzer");
                System.out.println(String.format("subcommand:%1$s", subCommand));
                System.out.println(String.format("localePrimary:%1$s", localePrimary));
                System.out.println(String.format("localeSecondary:%1$s", localeSecondary));
                System.out.println(String.format("resourceFolderOverride:%1$s", resourceFolderOverride));

                if (subCommand.equals("merge"))
                {
                    lra.shouldMerge = true;
                }

                lra.compareLocales(localePrimary, localeSecondary, resourceFolderOverride);
            }
        }
        catch (Exception ex)
        {
            System.out.println("Error running command:");
            ex.printStackTrace();
        }
        return blnFoundCommands;
    }

}
