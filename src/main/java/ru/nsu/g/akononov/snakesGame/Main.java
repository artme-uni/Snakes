package ru.nsu.g.akononov.snakesGame;

import ru.nsu.g.akononov.snakesGame.node.Node;
import ru.nsu.g.akononov.snakesGame.presenter.Presenter;
import ru.nsu.g.akononov.snakesGame.view.GUI;

import java.net.SocketException;

public class Main {

    public static void main(String[] args) throws SocketException {
        GUI view = new GUI();
        Node logic = new Node();
        Presenter presenter = new Presenter(logic,
                view.getGamePanel(),
                view.getGamePanel(),
                view.getMenuPanel(),
                view.getMenuPanel());

        view.setController(logic);
        logic.addPresenter(presenter, presenter);
    }
}