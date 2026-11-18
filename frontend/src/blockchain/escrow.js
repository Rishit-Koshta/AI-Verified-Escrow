
import { BrowserProvider, Contract } from "ethers";
import artifact from "./EscrowArtifact.json";
import {
  CONTRACT_ADDRESS,
  HARDHAT_CHAIN_ID,
  HARDHAT_NETWORK,
} from "./config";

export async function connectWallet() {
  if (!window.ethereum) {
    throw new Error("MetaMask is not installed.");
  }

  try {
    await window.ethereum.request({
      method: "wallet_switchEthereumChain",
      params: [{ chainId: HARDHAT_CHAIN_ID }],
    });
  } catch (error) {
    if (error.code === 4902) {
      await window.ethereum.request({
        method: "wallet_addEthereumChain",
        params: [HARDHAT_NETWORK],
      });
    } else {
      throw error;
    }
  }

  const accounts = await window.ethereum.request({
    method: "eth_requestAccounts",
  });

  const provider = new BrowserProvider(window.ethereum);
  const signer = await provider.getSigner();

  return {
    account: accounts[0],
    provider,
    signer,
  };
}

export async function getEscrowContract(signerOrProvider) {
  return new Contract(
    CONTRACT_ADDRESS,
    artifact.abi,
    signerOrProvider
  );
}

export async function getEscrowDetails(contract) {
  const [
    client,
    freelancer,
    arbitrator,
    amount,
    status,
    funded,
    submitted,
    approved,
    released,
    refunded,
    disputed,
  ] = await Promise.all([
    contract.client(),
    contract.freelancer(),
    contract.arbitrator(),
    contract.amount(),
    contract.status(),
    contract.funded(),
    contract.submitted(),
    contract.approved(),
    contract.released(),
    contract.refunded(),
    contract.disputed(),
  ]);

  return {
    client,
    freelancer,
    arbitrator,
    amount,
    status: Number(status),
    funded,
    submitted,
    approved,
    released,
    refunded,
    disputed,
  };
}
