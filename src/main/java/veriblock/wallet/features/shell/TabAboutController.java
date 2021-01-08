// VeriBlock GUI Wallet
// Copyright 2017-2021 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package veriblock.wallet.features.shell;

import veriblock.wallet.core.*;
import veriblock.wallet.core.locale.LocaleManager;
import veriblock.wallet.core.locale.LocaleModule;
import veriblock.wallet.features.LocaleModuleResource;
import veriblock.wallet.uicommon.ControlHelper;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

public class TabAboutController extends BaseController  {

    public Label lblVersion;
    public Label lblBuildDate;
    public Label lblAppName;

    public void initialize() {
    }

    private LocaleModule _localeModule;
    public void init()
    {
        setLocale();

        ControlHelper.setImage(imgName, "VeriBlock_Logo_Rev.png");

    }

    public void dispose() {

    }

    public void setLocale() {
        _localeModule = LocaleManager.getInstance().getModule(LocaleModuleResource.TabAbout);
        _appContext.UIManager.setTitle(_localeModule.getString("main.title"));
        lblAppName.setText(_localeModule.getString("label.appName"));

        this.lblVersion.setText(
                String.format(_localeModule.getString("label.version"), Constants.PROGRAM_VERSION));

        Date classBuildTime = getClassBuildTime();
        this.lblBuildDate.setText(
                String.format(_localeModule.getString("label.dateCompiled")
                        ,classBuildTime.toString()));
    }

    public javafx.scene.image.ImageView imgName;

    private static Date getClassBuildTime() {
        //https://stackoverflow.com/questions/3336392/java-print-time-of-last-compilation
        Date classBuildTime = null;
        Class<?> currentClass = new Object() {}.getClass().getEnclosingClass();
        URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
        if (resource != null) {
            if (resource.getProtocol().equals("file")) {
                try {
                    classBuildTime = new Date(new File(resource.toURI()).lastModified());
                } catch (URISyntaxException ignored) {
                }
            } else if (resource.getProtocol().equals("jar")) {
                String path = resource.getPath();
                classBuildTime = new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
            }
        }
        return classBuildTime;
    }

    public void click_Link(ActionEvent actionEvent)
    {
        String strUrl = "https://www.veriblock.org/";
       //open url
        Utils.openLink(strUrl);
    }

}
