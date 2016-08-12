import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by yiyang on 2016/8/11.
 */
public class AdbTool {
    private JPanel pnlRoot;
    private JLabel labInfo;
    private JTextArea txtInfo1_5;
    private JTextField txtAdbPath;
    private JButton btnSetAdbEnv;
    private JButton btnGetAdbPath;
    private JTextField txtDeviceSn;
    private JButton btnGetDeviceSn;
    private JTextArea txtInfo6;
    private JTextField txtDeviceIP;
    private JButton btnGetDeviceIP;
    private JTextArea a7設定設備接聽TextArea;
    private JButton btnSetListenPort;
    private JTextArea a8ADB由網路連線至設備TextArea;
    private JButton btnConnectDevice;
    private JTextArea a9拔除USB連結線完成設定TextArea;
    private JButton btnDisconnectDevice;
    private JButton btnKillAdbServer;
    private JButton btnPingIP;

    public AdbTool() {
        btnGetAdbPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check environment setting
                if (isAdbInSetupPath()) txtAdbPath.setText(getADBPath());

                    //If no setting, check default path
                else if (isAdbInDefPath()) {
                    txtAdbPath.setText(getLocalAppDataPath()
                            + "\\Android\\sdk\\platform-tools");
                    btnSetAdbEnv.setEnabled(true);
                } else {

                    //If no default, search drive c:
                    if (JOptionPane.showConfirmDialog(pnlRoot, "要搜尋C槽嗎？", "請問", JOptionPane.YES_NO_OPTION)
                            == JOptionPane.YES_OPTION) {
                        String path = searchCforAdb();

                        if (!path.equals("")) {
                            txtAdbPath.setText(path);
                            btnSetAdbEnv.setEnabled(true);
                            JOptionPane.showMessageDialog(pnlRoot, path + "\n找完了。");

                        } else
                            //Ask user
                            JOptionPane.showMessageDialog(pnlRoot, "C槽找不到adb.exe，請設定adb.exe所在目錄");

                    }
                }
            }
        });

        btnSetAdbEnv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = txtAdbPath.getText().trim();
                if (isFileExist(path + "\\adb.exe")) {
                    setAdbEnv(path);
                    JOptionPane.showMessageDialog(pnlRoot, setAdbEnv(path) + "\n設定了ADB_PATH環境變數");
                } else {
                    JOptionPane.showMessageDialog(pnlRoot, "找不到adb.exe，請設定adb.exe所在目錄");
                }
            }
        });

        btnGetDeviceSn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = getDeviceSn(txtAdbPath.getText());
                if (!path.equals("")) {
                    txtDeviceSn.setText(path);
                    JOptionPane.showMessageDialog(pnlRoot, path + "\n查完了");

                } else
                    JOptionPane.showMessageDialog(pnlRoot, "查不到，檢查USB連線後再試試");

            }
        });
        btnGetDeviceIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = getDeviceIP(txtAdbPath.getText(), txtDeviceSn.getText());

                if (!ip.equals("")) {
                    txtDeviceIP.setText(ip);
                    JOptionPane.showMessageDialog(pnlRoot, ip + "\n查完了");

                } else
                    JOptionPane.showMessageDialog(pnlRoot, "查不到IP，是不是沒有WIFI?");

            }
        });
        btnSetListenPort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(pnlRoot,
                        setDeviceListenPort(txtAdbPath.getText(), txtDeviceSn.getText(), 5555));
            }
        });
        btnConnectDevice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(pnlRoot,
                        connectDevice(txtAdbPath.getText(), txtDeviceIP.getText()));

            }
        });
        btnDisconnectDevice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(pnlRoot,
                        disconnectDevice(txtAdbPath.getText(), txtDeviceIP.getText()));

            }
        });
        btnKillAdbServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(pnlRoot,
                        killAdbServer(txtAdbPath.getText()));
            }
        });
        btnPingIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(pnlRoot,
                        pingIP(txtDeviceIP.getText()));
            }
        });
    }

    private static String pingIP(String ip) {

        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec("ping " + ip);
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "Windows-950").trim();
            if ((result.contains("要求等候逾時")) && (!result.contains("回覆自")))
                return "Ping不到耶，檢查一下網路吧!!";
            else if ((result.contains("要求等候逾時")) && (result.contains("回覆自")))
                return "網路不穩耶，檢查一下網路吧!!";
            else if ((!result.contains("要求等候逾時")) && (result.contains("回覆自")))
                return result + "\nPing到了!";

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String setDeviceListenPort(String adbPath, String devSn, int port) {

        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec(adbPath + "\\adb.exe -s " + devSn + " tcpip " + port);
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "ASCII").trim();
            if (result.equals("")) return "沒反應耶!!";

            return result + "\n設定了!";

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getDeviceIP(String adbPath, String devSn) {

        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec(adbPath + "\\adb.exe -s " + devSn + " shell ip -f inet addr show wlan0");
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "ASCII");
            result = result.trim();
            if (result.equals("")) return "";

            result = result.split("\r\n")[1].split(" brd ")[0].split("/")[0].replace("inet", "").trim();

            return result;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";

    }

    private static boolean isFileExist(String path) {
        if (path.equals("")) return false;

        File f = new File(path);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Android Studio WIFI 連線輔助工具@yiyang.edu.tw");
        frame.setContentPane(new AdbTool().pnlRoot);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static String setAdbEnv(String adbPath) {
        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec("setx ADB_PATH \"" + adbPath + "\"");
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "Windows-950");
            return result;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getDeviceSn(String adbPath) {
        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec(adbPath + "\\adb.exe devices");
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "ASCII");
            String[] tmp = result.split("\r\n");
            tmp = tmp[1].split("\t");

            return tmp[0];

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String searchCforAdb() {
        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec("CMD /C \"dir /s/a/b c:\\adb.exe\"");
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "UTF-8");
            result = result.substring(0, result.lastIndexOf("\\adb.exe"));
            return result;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";

    }

    private static boolean isAdbInSetupPath() {
        String path = getADBPath();
        if (path.equals("")) return false;

        File f = new File(path + "\\adb.exe");
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
    }

    private static String getADBPath() {
        return getEnvSetting("ADB_PATH");
    }

    private static boolean isAdbInDefPath() {
        String path = getLocalAppDataPath();
        if (path == "") return false;

        File f = new File(path + "\\Android\\sdk\\platform-tools\\adb.exe");
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
    }

    private static String getLocalAppDataPath() {
        return getEnvSetting("LOCALAPPDATA");
    }

    private static String getEnvSetting(String token) {
        Map<String, String> env = System.getenv();
        return env.get(token) == null ? "" : env.get(token);
    }

    private static String connectDevice(String adbPath, String ip) {

        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec(adbPath + "\\adb.exe connect " + ip);
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "ASCII");
            result = result.trim();
            if (result.indexOf("connected") == -1) return result + "\n哇，連不上耶!!\nPing看看網路通不通。";

            return result + "\n連上了!";

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String disconnectDevice(String adbPath, String ip) {

        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec(adbPath + "\\adb.exe disconnect " + ip);
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "ASCII");
            result = result.trim();
            if (result.indexOf("disconnected") == -1) return result + "\n哇，斷不了耶!!\n重新開機看看。";

            return result + "\n斷了!";

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String killAdbServer(String adbPath) {

        Process p = null;
        byte[] data;
        try {
            p = Runtime.getRuntime().exec(adbPath + "\\adb.exe kill-server");
            p.waitFor();
            data = new byte[2048];
            p.getInputStream().read(data);
            String result = new String(data, "ASCII");
            result = result.trim();

            return result + "\n重設了!再試試吧！";

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

}
