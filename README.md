# shogi-api-server

将棋のネット対戦ができる予定ですが、現状はAPIの疎通確認用のモックだけあります

言語はScala、パッケージマネージャはsbt、HTTPのフレームワークはakka-httpを使っています。

APIの起動や開発のためにsbtを適当にインストールする必要があります。(`$ brew install sbt`)
sbtやscalaのバージョンはプロジェクトの中で指定されたものが適当に利用されるので、適当にインストールして大丈夫です。

起動: 

```
$ cd path/to/shogi-api-server
$ sbt run
[info] welcome to sbt 1.3.13 (Oracle Corporation Java 1.8.0_192)
[info] loading settings for project global-plugins from plugins.sbt ...
[info] loading global plugins from /Users/miy/.sbt/1.0/plugins
[info] loading settings for project shogi-api-server-build-build-build from metals.sbt ...
[info] loading project definition from path/to/shogi-api-server/project/project/project
[info] loading settings for project shogi-api-server-build-build from metals.sbt ...
[info] loading project definition from path/to/shogi-api-server/project/project
[success] Generated .bloop/shogi-api-server-build-build.json
[success] Total time: 0 s, completed Sep 9, 2020 10:32:56 AM
[info] loading settings for project shogi-api-server-build from plugins.sbt,metals.sbt ...
[info] loading project definition from path/to/shogi-api-server/project
[success] Generated .bloop/shogi-api-server-build.json
[success] Total time: 1 s, completed Sep 9, 2020 10:32:58 AM
[info] loading settings for project root from build.sbt ...
[info] set current project to shogi-api-server (in build file:path/to/shogi-api-server/)
[info] running Main 
SLF4J: A number (1) of logging calls during the initialization phase have been intercepted and are
SLF4J: now being replayed. These are subject to the filtering rules of the underlying logging system.
SLF4J: See also http://www.slf4j.org/codes.html#replay
Server online at http://localhost:8080/
Press RETURN to stop...

```

接続の確認:

curlとかhttpieとかブラウザとかで `GET http://localhost:8080/shogi` にアクセス

```
$ http get :8080/shogi
HTTP/1.1 200 OK
Content-Length: 1212
Content-Type: application/json
Date: Wed, 09 Sep 2020 01:33:04 GMT
Server: akka-http/10.2.0

{
    "shiaiStatus": "Player1",
    "shogiban": {
        "hiyoko1": {
            "koma": "Hiyoko",
            "loaction": {
                "tate": 2,
                "tpe": "onShogiban",
                "yoko": 1
            }
        },
        "hiyoko2": {
            "koma": "Hiyoko",
            "loaction": {
                "tate": 1,
                "tpe": "onShogiban",
                "yoko": 1
            }
        },
        "kirin1": {
            "koma": "Kirin",
            "loaction": {
                "tate": 3,
                "tpe": "onShogiban",
                "yoko": 2
            }
        },
        "kirin2": {
            "koma": "Kirin",
            "loaction": {
                "tate": 0,
                "tpe": "onShogiban",
                "yoko": 0
            }
        },
        "lion1": {
            "koma": "Lion",
            "loaction": {
                "tate": 3,
                "tpe": "onShogiban",
                "yoko": 1
            }
        },
        "lion2": {
            "koma": "Lion",
            "loaction": {
                "tate": 0,
                "tpe": "onShogiban",
                "yoko": 1
            }
        },
        "zou1": {
            "koma": "Zou",
            "loaction": {
                "tate": 3,
                "tpe": "onShogiban",
                "yoko": 0
            }
        },
        "zou2": {
            "koma": "Zou",
            "loaction": {
                "tate": 0,
                "tpe": "onShogiban",
                "yoko": 2
            }
        }
    }
}
```


終了: コンソールでリターンを押せばとまります

異常終了した時: サーバーソケットの8080番が開きっぱなしになっていると思うので、 `lsof -i:8080`, `kill [プロセスの番号]` とか

なんか変なんだけど: ポート8080が取得出来なかった時に適切にエラーにせず起動しぱなしにしているので、多分そのへん
