
import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

export default buildModule("EscrowModule", (m) => {
  const freelancer = m.getParameter("freelancer");
  const arbitrator = m.getParameter("arbitrator");

  // Demo escrow amount: 0.01 ETH in wei
  const amountWei = 10000000000000000n;

  const escrow = m.contract("Escrow", [
    freelancer,
    arbitrator,
    amountWei,
  ]);

  return { escrow };
});
