/*
    Vox Tuiter

    Desarrollado por:
        - Nader Abu Fakhr (@naderst)
        - Moisés Moussa (@mdsse)

    GitHub: https://github.com/naderst/voxtuiter

    UCAB Guayana - Puerto Ordaz, Edo Bolívar. Venezuela
 */
package ve.edu.ucab.voxtuiter.app;

import java.util.ArrayList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.User;

enum Sitios {
    MENU, TIMELINE, TRENDSTITLES, TRENDS, MY_PROFILE, PROFILE,
    PROFILE_TWEETS, MENTIONS, SEARCH, MESSAGES, SENT_MESSAGES
}

/**
 * La clase AppMain es una abstracción para manejar el flujo de la App
 */
public class AppMain {
    /**
     * Objeto que se encarga de realizar el trabajo "sucio" de la App, brindando una interfaz
     * para los métodos básicos de la App.
     */
    private MainActivity mainActivity;
    /**
     * Objeto que permite utilizar las primitivas de Twitter de una manera sencilla.
     * No es más que un wrapper de twitter4j.
     */
    private TwitterManager twitter;

    private Sitios ubicacion;

    AppMain(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        twitter = new TwitterManager(mainActivity);
    }

    /**
     * Evento que se ejecuta cuando la aplicación está lista para empezar
     */
    public void onInit() {
        ubicacion = Sitios.MENU;
        ResponseList<Status> timeline = null;
        List<Status> trends = null;
        Trend[] trendsTitles = null;
        ArrayList<String> matches = null;
        ResponseList<User> users = null;
        ResponseList<DirectMessage> directMessages = null, sentDirectMessages = null;
        boolean flagRepeat = false;
        boolean flagEnd = false;
        String command = "";
        int i = 0, j = 0;
        long userId = 0;

        /*
        Ciclo infinito que lleva el control de todos los comandos de la aplicación que indica
        el usuario y administra el flujo de datos de toda la aplicación
        */
        while (true) {
            if (!flagRepeat) {
                if ((ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.TRENDSTITLES || ubicacion == Sitios.TRENDS) && !flagEnd) {
                    if ((matches = mainActivity.listenSpeech(5000)) != null)
                        command = matches.get(0);
                    else
                        command = "siguiente";
                } else {
                    mainActivity.speak("Diga un comando");
                    matches = mainActivity.listenSpeech(15000);
                    command = matches.get(0);
                }
            } else if (flagRepeat)
                command = "repetir";

            /*
            Comando de voz para repetir el último  mensaje directo, tweet, trending topic,
            meción o resultado de búsqueda
            */
            if (command.equals("repetir")) {
                switch (ubicacion) {
                    case TRENDSTITLES:
                        mainActivity.speak("La tendencia número" + (i + 1) + "es la siguiente:" + trendsTitles[i].getName());
                        break;
                    case TRENDS:
                        mainActivity.speak(trends.get(j).getUser().getName() + " dijo, " + trends.get(j).getText());
                        break;
                    case PROFILE:
                        twitter.profile(userId);
                        break;
                    case MY_PROFILE:
                        twitter.myProfile();
                        break;
                    case SEARCH:
                        mainActivity.speak("Nombre: " + users.get(i).getName() + ". @" + users.get(i).getScreenName());
                        break;
                    case MESSAGES:
                        mainActivity.speak(directMessages.get(i).getSender().getName() + " escribió: " + directMessages.get(i).getText());
                        mainActivity.speak(" Este mensaje fue escrito el: " + directMessages.get(i).getCreatedAt());
                        break;
                    case SENT_MESSAGES:
                        mainActivity.speak("Mensaje enviado a " + sentDirectMessages.get(i).getRecipient().getName() + ". El mensaje dice: " + sentDirectMessages.get(i).getText());
                        mainActivity.speak(" Este mensaje fue escrito el: " + sentDirectMessages.get(i).getCreatedAt());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                        else {
                            mainActivity.speak("Comando no disponible.");
                            continue;
                        }
                        break;
                }
                if (ubicacion != Sitios.PROFILE || ubicacion != Sitios.MY_PROFILE)
                    flagRepeat = false;
                continue;
            }

            /*
            Comando de voz para que lea el timeline del usuario
            cuya sesión esta iniciada en la aplicación
            */
            if (command.equals("leer")) {
                ubicacion = Sitios.TIMELINE;
                i = 0;
                if ((timeline = twitter.getTimeLine()) != null) {
                    flagEnd = false;
                    mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                }
                continue;
            }

            /*
            Comando de voz para obtener los top 10 trending topics de Venezuela
            */
            if (command.equals("tendencias")) {
                if (ubicacion != Sitios.TRENDS)
                    i = 0;

                ubicacion = Sitios.TRENDSTITLES;
                if ((trendsTitles = twitter.getTrendsTitles()) != null) {
                    flagEnd = false;
                    mainActivity.speak("La tendencia número" + (i + 1) + "es la siguiente:" + trendsTitles[i].getName());
                }
                continue;
            }

            /*
            Comando de voz para obtener las menciones más recientes
            del usuario cuya sesión está iniciada
            */
            if (command.equals("menciones")) {
                ubicacion = Sitios.MENTIONS;
                i = 0;
                if ((timeline = twitter.getMentions()) != null) {
                    flagEnd = false;
                    mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                }
                continue;
            }

            /*
            Comando de voz para obtener los mensajes directos recibidos más recientes
            del usuario cuya sesión está iniciada
            */
            if (command.equals("mensajes")) {
                ubicacion = Sitios.MESSAGES;
                i = 0;
                if ((directMessages = twitter.getDirectMessages()) != null) {
                    flagEnd = false;
                    mainActivity.speak(directMessages.get(i).getSender().getName() + " escribió: " + directMessages.get(i).getText());
                    mainActivity.speak(" Este mensaje fue escrito el: " + directMessages.get(i).getCreatedAt());
                }
                continue;
            }

            /*
            Comando de voz para obtener los mensajes directos enviados más recientes
            del usuario cuya sesión está iniciada
            */
            if (command.equals("mensajes enviados")) {
                ubicacion = Sitios.SENT_MESSAGES;
                i = 0;
                if ((sentDirectMessages = twitter.getSentDirectMessages()) != null) {
                    flagEnd = false;
                    mainActivity.speak("Mensaje enviado a " + sentDirectMessages.get(i).getRecipient().getName() + ". El mensaje dice: " + sentDirectMessages.get(i).getText());
                    mainActivity.speak(" Este mensaje fue escrito el: " + sentDirectMessages.get(i).getCreatedAt());
                }
                continue;
            }

            /*
            Comando de voz para enviar un nuevo mensaje directo a un usuario indicado
            */
            if (command.equals("mensaje nuevo")) {
                if (ubicacion == Sitios.MESSAGES) {
                    String recipientUser, text;
                    mainActivity.speak("¿A quién desea enviar el mensaje?");
                    if (!(recipientUser = mainActivity.listenSpeech(15000).get(0)).equals("cancelar")) {
                        mainActivity.speak("Diga su mensaje:");
                        if (!(text = mainActivity.listenSpeech(15000).get(0)).equals("cancelar"))
                            twitter.sendDirectMessage(recipientUser, text);
                        else
                            mainActivity.speak("El mensaje directo se ha cancelado.");
                    } else
                        mainActivity.speak("El mensaje directo se ha cancelado.");
                    continue;
                }
                continue;
            }

            /*
            Comando de voz para pasar al mensaje directo, tweet, trending topic,
            meción o resultado de búsqueda siguiente
            */
            if (command.equals("siguiente")) {
                switch (ubicacion) {
                    case TRENDSTITLES:
                        if (trendsTitles.length > (i + 1)) {
                            i++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en la última tendencia cargada.");
                            flagEnd = true;
                        }
                        break;
                    case TRENDS:
                        if (trends.size() > (j + 1)) {
                            j++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en el último tweet cargado de la tendencia actual.");
                            flagEnd = true;
                        }
                        break;
                    case SEARCH:
                        if (users.size() > (i + 1)) {
                            i++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en el último usuario encontrado en su búsqueda.");
                            flagEnd = true;
                        }
                        break;
                    case MESSAGES:
                        if (directMessages.size() > (i + 1)) {
                            i++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en el último mensaje directo recibido que se ha cargado.");
                            flagEnd = true;
                        }
                        break;
                    case SENT_MESSAGES:
                        if (sentDirectMessages.size() > (i + 1)) {
                            i++;
                            flagRepeat = true;
                        } else {
                            mainActivity.speak("Usted está en el último mensaje directo enviado que se ha cargado.");
                            flagEnd = true;
                        }
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            if (timeline.size() > (i + 1)) {
                                i++;
                                flagRepeat = true;
                            } else {
                                mainActivity.speak("Usted está en el último tweet cargado.");
                                flagEnd = true;
                            }
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para regresar a un mensaje directo, tweet, trending topic,
            meción o resultado de búsqueda anterior
            */
            if (command.equals("anterior")) {
                switch (ubicacion) {
                    case TRENDSTITLES:
                        if (i > 0) {
                            i--;
                            flagRepeat = true;
                        } else
                            mainActivity.speak("Usted está en la tendencia cargada más reciente.");
                        break;
                    case TRENDS:
                        if (j > 0) {
                            j--;
                            flagRepeat = true;
                        } else
                            mainActivity.speak("Usted está en el tweet cargado más reciente de la tendencia actual.");
                        break;
                    case SEARCH:
                        if (i > 0) {
                            i--;
                            flagRepeat = true;
                        } else
                            mainActivity.speak("Usted está en el primer usuario encontrado en su búsqueda.");
                        break;
                    case MESSAGES:
                        if (i > 0) {
                            i--;
                            flagRepeat = true;
                        } else
                            mainActivity.speak("Usted está en el mensaje directo recibido más reciente.");
                        break;
                    case SENT_MESSAGES:
                        if (i > 0) {
                            i--;
                            flagRepeat = true;
                        } else
                            mainActivity.speak("Usted está en el mensaje directo enviado más reciente.");
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            if (i > 0) {
                                i--;
                                flagRepeat = true;
                            } else
                                mainActivity.speak("Usted está en el tweet cargado más reciente.");
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para hacer un retweet
            */
            if (command.equals("retweet")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.retweet(trends.get(j).getId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.retweet(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para marcar como favorito un tweet indicado
            */
            if (command.equals("favorito")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.fav(trends.get(j).getId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.fav(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para quitar un tweet indicado de favoritos
            */
            if (command.equals("quitar de favoritos")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.removeFav(trends.get(j).getId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.removeFav(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para responder un tweet indicado
            */
            if (command.equals("responder")) {
                mainActivity.speak("Indique su respuesta");
                String reply;
                if (!(reply = mainActivity.listenSpeech(15000).get(0)).equals("cancelar"))
                    switch (ubicacion) {
                        case TRENDS:
                            twitter.reply(trends.get(j).getId(), reply, trends.get(j).getUser().getScreenName());
                            break;
                        case MESSAGES:
                            twitter.sendDirectMessage(directMessages.get(i).getSenderId(), reply);
                            break;
                        default:
                            if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                                twitter.reply(timeline.get(i).getId(), reply, timeline.get(i).getUser().getScreenName());
                            else
                                mainActivity.speak("Comando no disponible.");
                            break;
                    }
                else
                    mainActivity.speak("La respuesta se ha cancelado.");
                continue;
            }

            /*
            Comando de voz que indica más información acerca de un tweet
            */
            if (command.equals("más información")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.moreInformation(trends.get(j).getId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.moreInformation(timeline.get(i).getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para ingresar al timeline de tweets de una tendencia indicada
            */
            if (command.equals("entrar")) {
                if (ubicacion == Sitios.TRENDSTITLES) {
                    ubicacion = Sitios.TRENDS;
                    QueryResult result;
                    if ((result = twitter.trendTweets(trendsTitles[i].getQuery(), trendsTitles[i].getName())) != null) {
                        trends = result.getTweets();
                        j = 0;
                        flagEnd = false;
                        mainActivity.speak(trends.get(j).getUser().getName() + " dijo, " + trends.get(j).getText());
                    }
                } else
                    mainActivity.speak("Comando no disponible.");
                continue;
            }

            /*
            Comando de voz para twittear un mensaje
            */
            if (command.equals("twittear")) {
                String text;
                mainActivity.speak("Diga su tweet");
                if (!(text = mainActivity.listenSpeech(15000).get(0)).equals("cancelar"))
                    twitter.tweet(text);
                else
                    mainActivity.speak("El tweet se ha cancelado.");
                continue;
            }

            /*
            Comando de voz para realizar una búsqueda en twitter
            */
            if (command.equals("buscar")) {
                ubicacion = Sitios.SEARCH;
                String text;
                i = 0;
                mainActivity.speak("Indique la búsqueda:");
                if (!(text = mainActivity.listenSpeech(15000).get(0)).equals("cancelar")) {
                    if ((users = twitter.search(text)) != null) {
                        flagEnd = false;
                        mainActivity.speak("Nombre: " + users.get(i).getName() + ". @" + users.get(i).getScreenName());
                    }
                } else
                    mainActivity.speak("La búsqueda se ha cancelado.");
                continue;
            }
            /*
            Comando de voz para indicar la información del perfil de un usuario indicado
            */
            if (command.equals("perfil")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.profile(userId = trends.get(j).getUser().getId());
                        break;
                    case SEARCH:
                        twitter.profile(userId = users.get(i).getId());
                        break;
                    case MESSAGES:
                        twitter.profile(userId = directMessages.get(i).getSenderId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.profile(userId = timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                ubicacion = Sitios.PROFILE;
                continue;
            }

            /*
            Comando de voz para indicar la información de perfil del usuario
            cuya sesión está iniciada en la aplicación
            */
            if (command.equals("mi perfil")) {
                ubicacion = Sitios.MY_PROFILE;
                twitter.myProfile();
                continue;
            }

            /*
            Comando de voz para solicitar seguir a un usuario indicado
            */
            if (command.equals("seguir")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.follow(trends.get(j).getUser().getId());
                        break;
                    case PROFILE:
                        twitter.follow(userId);
                        break;
                    case SEARCH:
                        twitter.follow(users.get(i).getId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.follow(timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para dejar de seguir a un usuario indicado
            */
            if (command.equals("no seguir")) {
                switch (ubicacion) {
                    case TRENDS:
                        twitter.unfollow(trends.get(j).getUser().getId());
                        break;
                    case PROFILE:
                        twitter.unfollow(userId);
                        break;
                    case SEARCH:
                        twitter.unfollow(users.get(i).getId());
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS)
                            twitter.unfollow(timeline.get(i).getUser().getId());
                        else
                            mainActivity.speak("Comando no disponible.");
                        break;
                }
                continue;
            }

            /*
            Comando de voz para mostrar los tweets de un usuario indicado
            */
            if (command.equals("historial de mensajes")) {
                switch (ubicacion) {
                    case TRENDS:
                        if ((timeline = twitter.userTimeLine(trends.get(j).getUser().getId())) == null)
                            continue;
                        break;
                    case PROFILE:
                        if ((timeline = twitter.userTimeLine(userId)) == null)
                            continue;
                        break;
                    case MY_PROFILE:
                        if ((timeline = twitter.myTimeLine()) == null)
                            continue;
                        break;
                    case SEARCH:
                        if ((timeline = twitter.userTimeLine(users.get(i).getId())) == null)
                            continue;
                        break;
                    case MESSAGES:
                        if ((timeline = twitter.userTimeLine(directMessages.get(i).getSenderId())) == null)
                            continue;
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.PROFILE_TWEETS || ubicacion == Sitios.MENTIONS) {
                            if ((timeline = twitter.userTimeLine(timeline.get(i).getUser().getId())) == null)
                                continue;
                        } else {
                            mainActivity.speak("Comando no disponible.");
                            continue;
                        }
                        break;
                }
                i = 0;
                flagEnd = false;
                ubicacion = Sitios.PROFILE_TWEETS;
                mainActivity.speak(timeline.get(i).getUser().getName() + " dijo, " + timeline.get(i).getText());
                continue;
            }

            /*
            Comando de voz para salir de la aplicación
            */
            if (command.equals("salir")) {
                mainActivity.speak("Gracias por su visita, vuelva pronto");
                System.exit(0);
            }

            /*
            Comando de voz para solicitar ayuda de los comandos disponibles actualmente
            */
            if (command.equals("ayuda")) {
                mainActivity.speak("Actualmente puede indicar los siguientes comandos disponibles:");
                switch (ubicacion) {
                    case TRENDSTITLES:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                           "repetir, entrar, siguiente y anterior.");
                        break;
                    case TRENDS:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                           "repetir, siguiente, anterior, retweet, responder, favorito, quitar de favoritos, perfil," +
                                           "historial de mensajes, seguir, no seguir y más información.");
                        break;
                    case PROFILE:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                "repetir, historial de mensajes, seguir y no seguir.");
                        break;
                    case MY_PROFILE:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                "repetir e historial de mensajes");
                        break;
                    case SEARCH:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                "repetir, siguiente, anterior, perfil, historial de mensajes, seguir y no seguir.");
                        break;
                    case MESSAGES:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                "repetir, siguiente, anterior, perfil, historial de mensajes, responder y mensaje nuevo.");
                        break;
                    case SENT_MESSAGES:
                        mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                "repetir, siguiente y anterior.");
                        break;
                    default:
                        if (ubicacion == Sitios.TIMELINE || ubicacion == Sitios.MENTIONS)
                            mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                    "repetir, siguiente, anterior, retweet, responder, favorito, quitar de favoritos, perfil," +
                                    "historial de mensajes, seguir, no seguir y más información.");
                        else if (ubicacion == Sitios.PROFILE_TWEETS) {
                            mainActivity.speak("Leer, tendencias, menciones, mensajes, mensajes enviados, buscar, mi perfil, twittear, salir, cerrar sesión," +
                                    "repetir, siguiente, anterior, retweet, responder, favorito, quitar de favoritos, seguir, no seguir y más información.");
                        }
                        break;
                }
                        mainActivity.speak("Un placer servirle de ayuda.");
                continue;
            }

            /*
            Comando de voz para cerrar la sesión en la aplicación
            */
            if (command.equals("cerrar sesión")) {
                    twitter.signOut();
                    mainActivity.speak("Sesión finalizada con éxito. Cerrando aplicación, gracias por visitarnos vuelva pronto");
                    System.exit(0);
                }

                mainActivity.speak("Comando inválido, vuelva a intentarlo.");
            }
    }
}