# ライブラリ

## ディレクトリ構成

```
.
├── README.md
├── src/       # ライブラリ
└── mise.toml  # ツールのバージョンとスクリプト管理
# 残りはビルド関連なので基本触れない
```

## 環境構築

親ディレクトリで既に `mise` のインストールが完了しているはずなので、環境構築は不要。

## コマンド

コードを整形する。

```sh
./gradlew spotlessApply
```

[Dokka](https://github.com/Kotlin/dokka) でドキュメントを生成する。`build/dokka/html` に出力される。

```sh
./gradlew dokkaHtml
```
