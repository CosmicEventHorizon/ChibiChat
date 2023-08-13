#!/data/data/com.termux/files/usr/bin/bash

yes | pkg update
yes | pkg upgrade

dependencies=("clang" "wget" "git" "cmake")

for dep in "${dependencies[@]}"; do
    pkg install -y "$dep"
done

pkg install -y python


InstallKoboldcpp() {
    echo "Installing Koboldcpp"
    git clone https://github.com/LostRuins/koboldcpp
    cd "$HOME/koboldcpp"
    make
    wget https://huggingface.co/Sosaka/Alpaca-native-4bit-ggml/resolve/main/ggml-alpaca-7b-q4.bin
    python koboldcpp.py ggml-alpaca-7b-q4.bin

}
DownloadModel() {
    cd "$HOME/koboldcpp"
    wget https://huggingface.co/Sosaka/Alpaca-native-4bit-ggml/resolve/main/ggml-alpaca-7b-q4.bin
    python koboldcpp.py ggml-alpaca-7b-q4.bin

}


if [ -d "$HOME/koboldcpp" ]; then
    if [ -f "$HOME/koboldcpp/ggml-alpaca-7b-q4.bin" ]; then
          cd "$HOME/koboldcpp"
          python koboldcpp.py ggml-alpaca-7b-q4.bin
    else
        DownloadModel
    fi
else
    InstallKoboldcpp
fi

