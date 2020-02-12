package com.android.internetradio.vodyasov.amr;

public enum UserAgent
{
    WINDOWS_MEDIA_PLAYER("Windows-Media-Player/11.0.5721.5145"),
    VLC("vlc 1.1.0-git-20100330-0003"),
    AIMP("BASS/2.4"),
    MOZILLA("Mozilla/5.0 (Windows NT 6.3; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0"),
    CHROME("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"),
    OPERA("Opera/9.80 (Windows NT 6.2; WOW64) Presto/2.12.388 Version/12.17"),
    SAFARI("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2"),
    IE("Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; ASU2JS; rv:11.0) like Gecko");

    private String agent;
    UserAgent(String agent)
    {
        this.agent = agent;
    }

    @Override
    public String toString()
    {
        return agent;
    }
}
