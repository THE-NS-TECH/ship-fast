# -----------------------------------------------------------------------------
# compute.tf
# Cria a VM Linux na Azure.
# -----------------------------------------------------------------------------

resource "azurerm_linux_virtual_machine" "shipfast_app" {
  name                = "shipfast-app-server"
  resource_group_name = azurerm_resource_group.shipfast_rg.name
  location            = azurerm_resource_group.shipfast_rg.location
  size                = var.vm_size
  admin_username      = var.admin_username
  network_interface_ids = [
    azurerm_network_interface.shipfast_nic.id,
  ]

  admin_ssh_key {
    username   = var.admin_username
    public_key = var.ssh_public_key
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }

  # custom_data precisa ser codificado em Base64 para Azure
  custom_data = base64encode(<<-EOF
                #!/bin/bash
                apt-get update
                apt-get install -y docker.io docker-compose
                systemctl enable docker
                systemctl start docker
                EOF
  )
}
