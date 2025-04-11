# コーディングスペース

問題を解くときに使うディレクトリ。

## ディレクトリ構成

```
.
├── README.md
├── src  # セットアップすると作成される
│   └── main
│       └── kotlin
│           └── agc001
│               └── a
│                   └── Main.kt  # 提出するコード
├── Main.org.kt  # ソースコードの雛形
└── mise.toml    # ツールのバージョンとスクリプト管理
# 残りはビルド関連なので基本触れない
```

## 環境構築

### 必要なもの

- [mise](https://mise.jdx.dev/): バージョン管理ツール
- [online-judge-tools](https://github.com/online-judge-tools/oj): テスト、提出などの自動化
    - 後ほどインストールする

### 手順

`mise.toml` を信頼し、ツール (Python) をインストールする。

```sh
mise trust
mise install
```

online-judge-tools をインストールする。

```sh
pip install online-judge-tools
```

次に AtCoder にログインする。本来は以下のコマンドでログインできるが、最近の AtCoder ページの仕様変更によりできなくなった [^1]。

[^1]: [Failed to log in after AtCoder adopted cloudflare CAPTCHAs · Issue #934 · online-judge-tools/oj](https://github.com/online-judge-tools/oj/issues/934#issuecomment-2755259417)

```sh
oj login atcoder # ※今は使えない※
```

対処法として、[key-moon/aclogin](https://github.com/key-moon/aclogin) を使うことを推奨する。詳しい使い方はリポジトリの `README.md` を参照。

```sh
pip install aclogin
aclogin
# ブラウザの開発者ツールで Cookie の REVEL_SESSION の値をコピーし、貼り付ける
```

## 問題を解く流れ

例として、[AGC001 A](https://atcoder.jp/contests/agc001/tasks/agc001_a) を解く場合のセットアップ方法を示す。

まずディレクトリを作る。引数は `<contest_id>/<problem_id>` を想定しているが、問題ごとにユニークであれば動く。

```sh
mise run p agc001/a # 準備 (prepare)
```

> [!NOTE]
> このコマンドは `coding/` 配下ならどこでも実行できるので、わざわざ `coding/` まで戻る必要はない。

これで `src/main/kotlin/agc001/a` ディレクトリが作成され、雛形がコピーされる。

次にこのディレクトリに移動し、サンプルケースをダウンロードする。引数は問題ページの URL。

```sh
cd src/main/kotlin/agc001/a
oj d https://atcoder.jp/contests/agc001/tasks/agc001_a
```

実装が完了したらテストを行い、問題なければ提出する。

```sh
mise run r # プログラム実行 (run)
mise run t # 自動テスト (test)
mise run s # 提出 (submit)
```

提出後は提出ページが自動でブラウザ上で開かれる。
