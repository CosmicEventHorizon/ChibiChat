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
    wget https://huggingface.co/concedo/rwkv-v4-169m-ggml/resolve/main/rwkv-169m-q4_0new.bin
    python koboldcpp.py rwkv-169m-q4_0new.bin

}


if [ ! -d "$HOME/koboldcpp" ]; then
    InstallKoboldcpp
else
    cd "$HOME/koboldcpp"
    python koboldcpp.py rwkv-169m-q4_0new.bin
fi
