# 競技プログラミング環境 in Kotlin

## ディレクトリ構成

```
.
├── README.md
├── coding/   # 問題を解く
├── library/  # ライブラリを管理する
└── mise.toml # ツールのバージョン管理
```

## 環境構築

### 必要なもの

- [mise](https://mise.jdx.dev/): バージョン管理ツール
- エディタ (IntelliJ IDEA を想定)

### 手順

`mise.toml` を信頼し、ツール (主にランタイムとコンパイラ) をインストールする。

```sh
mise trust
mise install
```

後は各ディレクトリ内の `README.md` を参照。

### IntelliJ IDEA の設定

まずこのディレクトリをプロジェクトとして開く。次に `./coding/build.gradle.kts` を右クリックし、"Link Gradle Project" を選択する。これで Gradle プロジェクトとして認識される。`./library/build.gradle.kts` にも同様にする。

> [!WARNING]
> Windows WSL 環境にて、IntelliJ IDEA 2024.3 でプロジェクトをビルドできないことを確認している。その場合は 2024.2 にダウングレードする。
> 参考: [Intellij installed in windows can't build projects in WSL. : IDEA-367587](https://youtrack.jetbrains.com/issue/IDEA-367587/Intellij-installed-in-windows-cant-build-projects-in-WSL.)
