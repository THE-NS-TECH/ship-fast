# -----------------------------------------------------------------------------
# iam.tf
# Define políticas de acesso e segurança
# -----------------------------------------------------------------------------

resource "digitalocean_ssh_key" "shipfast_key" {
  name       = "ShipFast-Dev-Key"
  public_key = var.ssh_public_key
}
