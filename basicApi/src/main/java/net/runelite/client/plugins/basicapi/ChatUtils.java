package net.runelite.client.plugins.basicapi;

import net.runelite.api.Client;
import net.runelite.client.plugins.iutils.MenuUtils;
import net.runelite.client.plugins.iutils.game.Game;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.plugins.iutils.ui.Chatbox;

import javax.inject.Inject;

public class ChatUtils {
    @Inject
    private Client client;

    @Inject
    private MenuUtils menu;

    @Inject
    private iUtils utils;

    @Inject
    private Game game;

    public Chatbox.ChatState chatState() {
        switch (game.widget(162, 559).nestedInterface()) {
            case -1:
                return Chatbox.ChatState.CLOSED;
            case 11:
                return Chatbox.ChatState.ITEM_CHAT;
            case 217:
                return Chatbox.ChatState.PLAYER_CHAT;
            case 231:
                return Chatbox.ChatState.NPC_CHAT;
            case 219:
                return Chatbox.ChatState.OPTIONS_CHAT;
            case 193:
                return Chatbox.ChatState.SPECIAL;
            case 229:
                return Chatbox.ChatState.MODEL;
            case 633:
                return Chatbox.ChatState.SPRITE;
            case 233:
                return Chatbox.ChatState.LEVEL_UP;
            case 270:
                return Chatbox.ChatState.MAKE;
            default:
                throw new IllegalStateException("unknown chat child " + game.widget(162, 562).nestedInterface());
        }
    }

    public void chooseOption(String part) {
        if (chatState() == Chatbox.ChatState.CLOSED) {
            throw new IllegalStateException("chat closed before option: " + part);
        }

        if (chatState() != Chatbox.ChatState.OPTIONS_CHAT) {
            throw new IllegalStateException("not an options chat, " + chatState());
        }

        for (var i = 0; i < game.widget(219, 1).items().size(); i++) {
            if (game.widget(219, 1, i).text() != null && game.widget(219, 1, i).text().contains(part)) {
                game.widget(219, 1, i).select();
                return;
            }
        }

        throw new IllegalStateException("no option " + part + " found");
    }

    public void continueChat() {
        switch (chatState()) {
            case CLOSED:
                throw new IllegalStateException("there's no chat");
            case OPTIONS_CHAT:
                throw new IllegalStateException("can't continue, this is an options chat");
            case PLAYER_CHAT:
                game.widget(217, 5).select();
                break;
            case NPC_CHAT:
                game.widget(231, 5).select();
                break;
            case ITEM_CHAT:
                game.widget(11, 4).select();
                break;
            case SPECIAL:
                game.widget(193, 0, 1).select();
                break;
            case MODEL:
                game.widget(229, 2).select();
                break;
            case SPRITE:
                game.widget(633, 0, 1).select();
                break;
            case LEVEL_UP:
                game.widget(233, 3).select();
                break;
            default:
                throw new IllegalStateException("unknown continue chat " + chatState());
        }
    }



    public enum ChatState {
        CLOSED,
        PLAYER_CHAT,
        NPC_CHAT,
        ITEM_CHAT,
        OPTIONS_CHAT,
        SPECIAL,
        MODEL,
        SPRITE,
        LEVEL_UP,
        MAKE
    }

}
