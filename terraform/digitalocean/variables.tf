# -----------------------------------------------------------------------------
# variables.tf
# Define as variáveis parametrizáveis para não ter hardcode de segredos
# -----------------------------------------------------------------------------

variable "do_token" {
  description = "Digital Ocean API Token"
  type        = string
  sensitive   = true
}

variable "region" {
  description = "Região onde a infraestrutura será criada"
  type        = string
  default     = "nyc3"
}

variable "droplet_size" {
  description = "Tamanho da máquina para a app"
  type        = string
  default     = "s-1vcpu-2gb"
}

variable "ssh_public_key" {
  description = "Chave SSH pública para acesso aos servidores"
  type        = string
}
