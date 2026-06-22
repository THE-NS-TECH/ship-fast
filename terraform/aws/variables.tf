# -----------------------------------------------------------------------------
# variables.tf
# Define as variáveis parametrizáveis para a AWS.
# -----------------------------------------------------------------------------

variable "aws_region" {
  description = "Região da AWS"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "Tipo de instância EC2 para a aplicação"
  type        = string
  default     = "t3.micro"
}

variable "ssh_public_key" {
  description = "Chave SSH pública para acesso à EC2"
  type        = string
}

variable "db_name" {
  description = "Nome do banco de dados MySQL"
  type        = string
  default     = "shipfast"
}

variable "db_user" {
  description = "Usuário do banco de dados MySQL"
  type        = string
  default     = "shipfast_user"
}

variable "db_password" {
  description = "Senha do banco de dados MySQL"
  type        = string
  sensitive   = true
}
