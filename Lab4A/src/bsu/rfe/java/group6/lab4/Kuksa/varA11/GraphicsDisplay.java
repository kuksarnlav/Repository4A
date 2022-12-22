package bsu.rfe.java.group6.lab4.Kuksa.varA11;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    private Double[][] graphicsData;// Список координат точек для построения графика
    private boolean showAxis = true, showMarkers = true; // Флаговые переменные, задающие правила отображения графика
    private double minX, maxX, minY, maxY; // Границы диапазона пространства, подлежащего отображению
    private double scale; // Используемый масштаб отображения
    private BasicStroke graphicsStroke, axisStroke, markerStroke; // Различные стили черчения линий
    private Font axisFont; // Различные шрифты отображения надписей

    public GraphicsDisplay() {
        setBackground(Color.WHITE); // Цвет заднего фона области отображения - белый
        // Сконструировать необходимые объекты, используемые в рисовании
        // Перо для рисования графика
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[] {1,1,2,1,1,1,4,1,2,1}, 0.0f); // ПОМЕНЯЛ
        // Перо для рисования осей координат
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.TRUETYPE_FONT, 36); // Шрифт для подписей осей координат
    }
    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData; // Сохранить массив точек во внутреннем поле класса
        repaint(); // Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
    }

    // Методы-модификаторы для изменения параметров отображения графика
    // Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g) {
        /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
          Эта функциональность - единственное, что осталось в наследство от
          paintComponent класса JPanel */
        super.paintComponent(g);

        // Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
        if (graphicsData == null || graphicsData.length == 0) return;

        // Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
        // Это необходимо для определения области пространства, подлежащей отображению
        // Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        // Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) { minY = graphicsData[i][1]; }
            if (graphicsData[i][1] > maxY) { maxY = graphicsData[i][1]; }
        }

        // Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y - сколько пикселов приходится на единицу длины по X и по Y
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        // Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
        scale = Math.min(scaleX, scaleY); // Выбираем за основу минимальный

        // Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
        if (scale == scaleX) {
            // Если за основу был взят масштаб по оси X, значит по оси Y делений меньше, т.е. подлежащий визуализации диапазон по Y будет меньше
            // высоты окна. Значит необходимо добавить делений, сделаем это так:
            // 1) Вычислим, сколько делений влезет по Y при выбранном масштабе - getSize().getHeight()/scale
            // 2) Вычтем из этого сколько делений требовалось изначально
            // 3) Набросим по половине недостающего расстояния на maxY и minY
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            // Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        // Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        // Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
        // Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующи
        // Первыми (если нужно) отрисовываются оси координат.
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas); // Затем отображается сам график

        // Затем (если нужно) отображаются маркеры точек, по которым строился график. !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (showMarkers) paintMarkers(canvas);

        // Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintGraphics(Graphics2D canvas) { // Отрисовка графика по прочитанным координатам
        canvas.setStroke(graphicsStroke); // Выбрать линию для рисования графика !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        canvas.setColor(Color.RED); // Выбрать цвет линии
        // Будем рисовать линию графика как путь, состоящий из множества сегментов (GeneralPath)
        // Начало пути устанавливается в первую точку графика, после чего прямой соединяется со следующими точками
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]); // Преобразовать значения (x,y) в точку на экране point
            if (i > 0) { // Не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else { // Первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics); // Отобразить график
    }

    protected void paintMarkers(Graphics2D canvas) { // Отображение маркеров точек, по которым рисовался график !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Шаг 1 - Установить специальное перо для черчения контуров маркеров
        canvas.setStroke(new BasicStroke(1));
        // Шаг 2 - Организовать цикл по всем точкам графика
        for (Double[] point : graphicsData) {
            if (Even(point[1]))
                canvas.setColor(Color.RED); // Выбрать черный цвета для контуров маркеров
            else
                canvas.setColor(Color.BLACK);
            Point2D.Double center = xyToPoint(point[0], point[1]); // Центр - в точке (x,y)
            canvas.draw(new Line2D.Double(center.x - 4, center.y, center.x - 1, center.y + 1));
            canvas.draw(new Line2D.Double(center.x - 1, center.y + 1, center.x, center.y + 4));
            canvas.draw(new Line2D.Double(center.x, center.y + 4, center.x + 1, center.y + 1));
            canvas.draw(new Line2D.Double(center.x + 1, center.y + 1, center.x + 4, center.y));
            canvas.draw(new Line2D.Double(center.x + 4, center.y, center.x + 1, center.y - 1));
            canvas.draw(new Line2D.Double(center.x + 1, center.y - 1, center.x, center.y - 4));
            canvas.draw(new Line2D.Double(center.x, center.y - 4, center.x - 1, center.y - 1));
            canvas.draw(new Line2D.Double(center.x - 1, center.y - 1, center.x - 4, center.y));
            // Начертить контур маркера
        }

/*        for (Double[] point : graphicsData) {
            //java.awt.geom.Arc2D
            Ellipse2D.Double marker = new Ellipse2D.Double(); // Инициализировать эллипс как объект для представления маркера
            // Эллипс будет задаваться посредством указания координат его центра и угла прямоугольника, в который он вписан
            Point2D.Double center = xyToPoint(point[0], point[1]); // Центр - в точке (x,y)
            Point2D.Double corner = shiftPoint(center, 3, 3); // Угол прямоугольника - отстоит на расстоянии (3,3)
            marker.setFrameFromCenter(center, corner); // Задать эллипс по центру и диагонали
            canvas.draw(marker); // Начертить контур маркера
        }*/
    }
    protected boolean Even(double y) {
        //marker paint
        int intFunc = (int) y;
            if( intFunc % 2 == 0)
                return true;
        return false;
    }

    protected void paintAxis(Graphics2D canvas) { // Метод, обеспечивающий отображение осей координат
        canvas.setStroke(axisStroke); // Установить особое начертание для осей
        canvas.setColor(Color.BLACK); // Оси рисуются чѐрным цветом
        canvas.setPaint(Color.BLACK); // Стрелки заливаются чѐрным цветом
        canvas.setFont(axisFont); // Подписи к координатным осям делаются специальным шрифтом
        // Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();


        if (minX <= 0.0 && maxX >= 0.0) { // Определить, должна ли быть видна ось Y на графике
            // Она должна быть видна, если левая граница показываемой области (minX) <= 0.0, а правая (maxX) >= 0.0
            // Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath(); // Стрелка оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY); // Установить начальную точку ломаной точно на верхний конец оси Y
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            // Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            // Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath(); // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
            Rectangle2D bounds = axisFont.getStringBounds("y", context); // Нарисовать подпись к оси Y
            Point2D.Double labelPos = xyToPoint(0, maxY); // Определить, сколько места понадобится для надписи "y"
            // Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
        }

        if (minY <= 0.0 && maxY >= 0.0) { // Определить, должна ли быть видна ось X на графике
            // Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0, а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath(); // Стрелка оси X
            // Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            // Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            // Вести левую часть стрелки в точку с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath(); // Замкнуть треугольник стрелки
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку

            // Нарисовать подпись к оси X
            // Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);

            // Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }
    // Метод-помощник, осуществляющий преобразование координат.  Оно необходимо, т.к. верхнему левому углу холста с координатам
    //(0.0, 0.0) соответствует точка графика с координатами (minX, maxY), где minX - это самое "левое" значение X, а maxY - самое "верхнее" значение Y.

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX; // Вычисляем смещение X от самой левой точки (minX)
        double deltaY = maxY - y; // Вычисляем смещение Y от точки верхней точки (maxY)
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }
    // Метод-помощник, возвращающий экземпляр класса Point2D.Double смещѐнный по отношению к исходному на deltaX, deltaY
    // К сожалению, стандартного метода, выполняющего такую задачу, нет.

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double(); // Инициализировать новый экземпляр точки
        // Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}