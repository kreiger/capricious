package com.linuxgods.kreiger.capricious.twitch.api;

public class Stream {
    private Long id;
    private Channel channel;
    private String game;
    private Preview preview;
    private int viewers;
    private String name;

    public long getId() {
        return id;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getGame() {
        return game;
    }

    public Preview getPreview() {
        return preview;
    }

    public int getViewers() {
        return viewers;
    }


    public static class Preview {
        private String large;
        private String medium;
        private String small;
        private String template;

        public String getLarge() {
            return large;
        }

        public String getMedium() {
            return medium;
        }

        public String getSmall() {
            return small;
        }

        public String getTemplate() {
            return template;
        }
    }
}
