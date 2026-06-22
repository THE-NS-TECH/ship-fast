# -----------------------------------------------------------------------------
# variables.tf
# Define as variáveis parametrizáveis para o GCP.
# -----------------------------------------------------------------------------

variable "gcp_project" {
  description = "ID do Projeto do GCP"
  type        = string
}

variable "gcp_region" {
  description = "Regiao do GCP"
  type        = string
  default     = "us-central1"
}

variable "gcp_zone" {
  description = "Zona do GCP"
  type        = string
  default     = "us-central1-a"
}

variable "machine_type" {
  description = "Tipo de máquina para a VM da aplicação"
  type        = string
  default     = "e2-micro"
}

variable "ssh_public_key" {
  description = "Chave SSH pública"
  type        = string
}

variable "ssh_user" {
  description = "Nome de usuário correspondente à chave SSH"
  type        = string
  default     = "ubuntu"
}

variable "db_password" {
  description = "Senha do banco de dados Cloud SQL MySQL"
  type        = string
  sensitive   = true
}
