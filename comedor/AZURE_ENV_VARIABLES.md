# Variables de Entorno para Azure

Para actualizar la configuración de Azure Storage en producción, necesitas configurar estas variables de entorno en tu App Service de Azure:

## Azure Storage Configuration

```
AZURE_STORAGE_CONNECTION_STRING=<Tu connection string de Azure Storage>

AZURE_STORAGE_CONTAINER_NAME=imagenesproductos
```

**Valores actuales:**
- Storage Account: `comedoruadesa`
- Container: `imagenesproductos`
- Connection String: Obtener desde Azure Portal > Storage Account > Access keys

## Cómo configurar en Azure Portal:

1. Ve a tu App Service en Azure Portal
2. En el menú lateral, selecciona **Configuration** (Configuración)
3. En la pestaña **Application settings**, haz clic en **+ New application setting**
4. Agrega cada variable con su valor correspondiente
5. Haz clic en **Save** para guardar los cambios
6. Haz clic en **Continue** para reiniciar la aplicación

## O usando Azure CLI:

```bash
az webapp config appsettings set --name <tu-app-name> --resource-group <tu-resource-group> --settings \
  AZURE_STORAGE_CONNECTION_STRING="<Tu connection string>" \
  AZURE_STORAGE_CONTAINER_NAME="imagenesproductos"
```

## Verificar el contenedor

Asegúrate de que el contenedor `imagenesproductos` existe en tu Storage Account `comedoruadesa`:

1. Ve a Storage Account > Containers
2. Si no existe, créalo con acceso privado (Blob)
