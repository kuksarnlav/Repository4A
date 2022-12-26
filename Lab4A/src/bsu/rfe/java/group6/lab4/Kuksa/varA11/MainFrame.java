package bsu.rfe.java.group6.lab4.Kuksa.varA11;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final int WIDTH = 800, HEIGHT = 600; // Начальные размеры окна приложения
    private JFileChooser fileChooser = null; // Объект диалогового окна для выбора файлов
    private JCheckBoxMenuItem showAxisMenuItem, showMarkersMenuItem; // Пункты меню
    private GraphicsDisplay display = new GraphicsDisplay(); // Компонент-отображатель графика
    private boolean fileLoaded = false; // Флаг, указывающий на загруженность данных графика
    public MainFrame() {
        super("Построение графиков функций на основе заранее подготовленных файлов"); // Вызов конструктора предка Frame
        setSize(WIDTH, HEIGHT); // Установка размеров окна
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2); // Отцентрировать окно приложения на экране
        setExtendedState(MAXIMIZED_BOTH); // Развѐртывание окна на весь экран
        JMenuBar menuBar = new JMenuBar(); // Создать и установить полосу меню
        setJMenuBar(menuBar); // Добавить пункт меню "Файл"
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);

        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") { // Создать действие по открытию файла
        public void actionPerformed(ActionEvent event) {
            if (fileChooser == null) {
                fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
            }
            if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                openGraphics(fileChooser.getSelectedFile());
        }
        };
        fileMenu.add(openGraphicsAction);// Добавить соответствующий элемент меню
        JMenu graphicsMenu = new JMenu("График"); // Создать пункт меню "График"
        menuBar.add(graphicsMenu);

    Action showAxisAction = new AbstractAction("Показывать оси координат") { // Создать действие для реакции на активацию элемента "Показывать оси координат"
    public void actionPerformed(ActionEvent event) {
        display.setShowAxis(showAxisMenuItem.isSelected());
        // свойство showAxis класса GraphicsDisplay истина, если элемент меню showAxisMenuItem отмечен флажком, и ложь - в противном случае
    }
    };
    showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction); // Добавить соответствующий элемент в меню
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true); // Элемент по умолчанию включен (отмечен флажком)
        // Повторить действия для элемента "Показывать маркеры точек"
        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent event) { // по аналогии с showAxisMenuItem
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };

        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true); // Элемент по умолчанию включен (отмечен флажком)
        graphicsMenu.addMenuListener(new GraphicsMenuListener()); // Зарегистрировать обработчик событий, связанных с меню "График"
        getContentPane().add(display, BorderLayout.CENTER); // Установить GraphicsDisplay в цент граничной компоновки
        }

protected void openGraphics(File selectedFile) { // Считывание данных графика из существующего файла
        try { // Шаг 1 - Открыть поток чтения данных, связанный с входным файловым потоком
        DataInputStream in = new DataInputStream(new
        FileInputStream(selectedFile));
/* Шаг 2 - Зная объѐм данных в потоке ввода можно вычислить,
 сколько памяти нужно зарезервировать в массиве: Всего байт в потоке - in.available() байт;
 Размер одного числа Double - Double.SIZE бит, или Double.SIZE/8 байт;
 Так как числа записываются парами, то число пар меньше в 2 раза
*/
        Double[][] graphicsData = new
        Double[in.available()/(Double.SIZE/8)/2][];
        int i = 0; // Шаг 3 - Цикл чтения данных (пока в потоке есть данные)
        while (in.available() > 0) {
        Double x = in.readDouble(); // Первой из потока читается координата точки X
        Double y = in.readDouble(); // Затем - значение графика Y в точке X
        graphicsData[i++] = new Double[] {x, y}; // Прочитанная пара координат добавляется в массив
        }
        if (graphicsData!=null && graphicsData.length > 0) { // Шаг 4 - Проверка, имеется ли в списке в результате чтения хотя бы одна пара координат
        fileLoaded = true; // Да - установить флаг загруженности данных
        display.showGraphics(graphicsData); // Вызывать метод отображения графика
        }
        in.close(); // Шаг 5 - Закрыть входной поток
        } catch (FileNotFoundException ex) {
            // В случае исключительной ситуации типа "Файл не найден" показать сообщение об ошибке
        JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
        return;
        } catch (IOException ex) { // В случае ошибки ввода из файлового потока показать сообщение об ошибке
        JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных",
        JOptionPane.WARNING_MESSAGE);
        return;
        }
    }
    public static void main(String[] args) { // Создать и показать экземпляр главного окна приложения
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    private class GraphicsMenuListener implements MenuListener { // Класс-слушатель событий, связанных с отображением меню
        public void menuSelected(MenuEvent e) { // Обработчик, вызываемый перед показом меню
// Доступность или недоступность элементов меню "График" определяется загруженностью данных
        showAxisMenuItem.setEnabled(fileLoaded);
        showMarkersMenuItem.setEnabled(fileLoaded);
    }
    public void menuDeselected(MenuEvent e) {} // Обработчик, вызываемый после того, как меню исчезло с экрана
    public void menuCanceled(MenuEvent e) {} // Обработчик, вызываемый в случае отмены выбора пункта меню (очень редкая ситуация)
}
}