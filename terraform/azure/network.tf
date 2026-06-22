# -----------------------------------------------------------------------------
# network.tf
# Define recursos de rede na Azure.
# -----------------------------------------------------------------------------

resource "azurerm_resource_group" "shipfast_rg" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_virtual_network" "shipfast_vnet" {
  name                = "shipfast-vnet"
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.shipfast_rg.location
  resource_group_name = azurerm_resource_group.shipfast_rg.name
}

resource "azurerm_subnet" "shipfast_subnet" {
  name                 = "shipfast-subnet"
  resource_group_name  = azurerm_resource_group.shipfast_rg.name
  virtual_network_name = azurerm_virtual_network.shipfast_vnet.name
  address_prefixes     = ["10.0.1.0/24"]
}

resource "azurerm_public_ip" "shipfast_pip" {
  name                = "shipfast-public-ip"
  location            = azurerm_resource_group.shipfast_rg.location
  resource_group_name = azurerm_resource_group.shipfast_rg.name
  allocation_method   = "Static"
}

resource "azurerm_network_security_group" "shipfast_nsg" {
  name                = "shipfast-nsg"
  location            = azurerm_resource_group.shipfast_rg.location
  resource_group_name = azurerm_resource_group.shipfast_rg.name

  security_rule {
    name                       = "SSH"
    priority                   = 1001
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "AppPort"
    priority                   = 1002
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8080"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }
}

resource "azurerm_network_interface" "shipfast_nic" {
  name                = "shipfast-nic"
  location            = azurerm_resource_group.shipfast_rg.location
  resource_group_name = azurerm_resource_group.shipfast_rg.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.shipfast_subnet.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.shipfast_pip.id
  }
}

resource "azurerm_network_interface_security_group_association" "nic_nsg_assoc" {
  network_interface_id      = azurerm_network_interface.shipfast_nic.id
  network_security_group_id = azurerm_network_security_group.shipfast_nsg.id
}
