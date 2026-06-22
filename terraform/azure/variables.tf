# -----------------------------------------------------------------------------
# variables.tf
# Define as variáveis parametrizáveis para a Azure.
# -----------------------------------------------------------------------------

variable "location" {
  description = "Região da Azure"
  type        = string
  default     = "East US"
}

variable "resource_group_name" {
  description = "Nome do Resource Group"
  type        = string
  default     = "shipfast-rg"
}

variable "vm_size" {
  description = "Tamanho da VM Azure"
  type        = string
  default     = "Standard_B1s"
}

variable "admin_username" {
  description = "Nome do usuário administrador na VM"
  type        = string
  default     = "azureuser"
}

variable "ssh_public_key" {
  description = "Chave SSH pública para acesso à VM"
  type        = string
}

variable "db_password" {
  description = "Senha do administrador para o MySQL Flexible Server"
  type        = string
  sensitive   = true
}
