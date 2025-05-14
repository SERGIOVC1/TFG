import os
import sys

def launch_attack(target):
    print("🚀 Iniciando ataque contra:", target)
    os.system("ping -n 5 " + target)  # Esto es solo una prueba. Reemplázalo con un ataque real.

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("❌ Error: No se especificó un objetivo.")
    else:
        launch_attack(sys.argv[1])
