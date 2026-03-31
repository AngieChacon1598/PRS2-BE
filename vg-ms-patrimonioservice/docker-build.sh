#!/bin/bash

# Script para rebuild y push de la imagen Docker
# Imagen: williams31/vg-ms-patrimonioservice:latest

set -e  # Detener en caso de error

echo "🐳 Iniciando rebuild de la imagen Docker..."
echo "📦 Imagen: williams31/vg-ms-patrimonioservice:latest"
echo ""

# Limpiar builds previos
echo "🧹 Limpiando builds previos..."
./mvnw clean

# Build de la imagen Docker
echo "🔨 Construyendo imagen Docker..."
docker build -t williams31/vg-ms-patrimonioservice:latest .

echo ""
echo "✅ Imagen construida exitosamente"
echo ""

# Preguntar si desea hacer push
read -p "¿Deseas hacer push a Docker Hub? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo "🚀 Haciendo push a Docker Hub..."
    docker push williams31/vg-ms-patrimonioservice:latest
    echo ""
    echo "✅ Push completado exitosamente"
    echo ""
    echo "📋 Para usar la imagen:"
    echo "   docker pull williams31/vg-ms-patrimonioservice:latest"
    echo "   docker run -d -p 5003:5003 williams31/vg-ms-patrimonioservice:latest"
else
    echo "⏭️  Push cancelado"
    echo ""
    echo "📋 Para hacer push manualmente:"
    echo "   docker push williams31/vg-ms-patrimonioservice:latest"
fi

echo ""
echo "🎉 Proceso completado"
