
import { useEffect, useState } from "react";
import {
  BrowserProvider,
  Contract,
  formatEther,
  parseEther,
} from "ethers";

import EscrowArtifact from "./contracts/Escrow.json";
import "./App.css";

const CONTRACT_ADDRESS =
  "0x5FbDB2315678afecb367f032d93F642f64180aa3";

const CHAIN_ID = "0x7a69"; // 31337

const ABI = EscrowArtifact.abi;

const STATUS_NAMES = [
  "Created",
  "Funded",
  "Submitted",
  "Approved",
  "Released",
  "Refunded",
  "Disputed",
];

function App() {
  const [account, setAccount] = useState("");
  const [contractData, setContractData] = useState(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const [provider, setProvider] = useState(null);
  const [signer, setSigner] = useState(null);
  const [escrow, setEscrow] = useState(null);

  async function connectWallet() {
    try {
      setMessage("");

      if (!window.ethereum) {
        throw new Error("Please install MetaMask.");
      }

      await window.ethereum.request({
        method: "eth_requestAccounts",
      });

      let chainId = await window.ethereum.request({
        method: "eth_chainId",
      });

      if (chainId !== CHAIN_ID) {
        await window.ethereum.request({
          method: "wallet_switchEthereumChain",
          params: [{ chainId: CHAIN_ID }],
        });
      }

      const walletProvider = new BrowserProvider(
        window.ethereum
      );

      const walletSigner = await walletProvider.getSigner();

      const walletAddress = await walletSigner.getAddress();

      const contract = new Contract(
        CONTRACT_ADDRESS,
        ABI,
        walletSigner
      );

      setProvider(walletProvider);
      setSigner(walletSigner);
      setEscrow(contract);
      setAccount(walletAddress);

      await loadContractData(contract);

      setMessage("Wallet connected successfully.");
    } catch (error) {
      console.error(error);
      setMessage(error.shortMessage || error.message);
    }
  }

  async function loadContractData(contract = escrow) {
    if (!contract) return;

    try {
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

      setContractData({
        client,
        freelancer,
        arbitrator,
        amount: formatEther(amount),
        status: STATUS_NAMES[Number(status)],
        funded,
        submitted,
        approved,
        released,
        refunded,
        disputed,
      });
    } catch (error) {
      console.error(error);
      setMessage("Unable to read contract data: " + error.message);
    }
  }

  async function executeTransaction(action, successMessage) {
    try {
      if (!escrow) {
        throw new Error("Connect your wallet first.");
      }

      setLoading(true);
      setMessage("Waiting for MetaMask confirmation...");

      const transaction = await action();

      setMessage("Transaction submitted: " + transaction.hash);

      await transaction.wait();

      await loadContractData();

      setMessage(successMessage);
    } catch (error) {
      console.error(error);
      setMessage(error.shortMessage || error.reason || error.message);
    } finally {
      setLoading(false);
    }
  }

  function deposit() {
  if (!contractData) return;

  executeTransaction(
    () => escrow.deposit({
      value: parseEther(contractData.amount),
    }),
    "Escrow funded successfully."
  );
}

  function submitWork() {
    executeTransaction(
      () => escrow.submitMilestone(),
      "Work submitted on the blockchain."
    );
  }

  function approveWork() {
    executeTransaction(
      () => escrow.approveMilestone(),
      "Milestone approved successfully."
    );
  }

  function releasePayment() {
    executeTransaction(
      () => escrow.releasePayment(),
      "Payment released to freelancer."
    );
  }

  function refundPayment() {
    executeTransaction(
      () => escrow.refund(),
      "Refund sent to client."
    );
  }

  function raiseDispute() {
    executeTransaction(
      () => escrow.raiseDispute(),
      "Dispute raised successfully."
    );
  }

  function resolveDispute(releaseToFreelancer) {
    executeTransaction(
      () => escrow.resolveDispute(releaseToFreelancer),
      releaseToFreelancer
        ? "Dispute resolved: payment released to freelancer."
        : "Dispute resolved: client refunded."
    );
  }

  useEffect(() => {
    if (!window.ethereum) return;

    const handleAccountsChanged = () => {
      setAccount("");
      setContractData(null);
      setEscrow(null);
      setSigner(null);
      setProvider(null);
      setMessage("Wallet account changed. Please reconnect.");
    };

    const handleChainChanged = () => {
      window.location.reload();
    };

    window.ethereum.on("accountsChanged", handleAccountsChanged);
    window.ethereum.on("chainChanged", handleChainChanged);

    return () => {
      window.ethereum.removeListener(
        "accountsChanged",
        handleAccountsChanged
      );
      window.ethereum.removeListener(
        "chainChanged",
        handleChainChanged
      );
    };
  }, []);

  const isClient =
    account && contractData &&
    account.toLowerCase() === contractData.client.toLowerCase();

  const isFreelancer =
    account && contractData &&
    account.toLowerCase() === contractData.freelancer.toLowerCase();

  const isArbitrator =
    account && contractData &&
    account.toLowerCase() === contractData.arbitrator.toLowerCase();

  return (
    <main className="app">
      <header>
        <h1>AI-Verified Blockchain Escrow</h1>
        <p>Secure payments through smart contracts</p>
      </header>

      <section className="panel">
        <h2>Wallet Connection</h2>

        <button onClick={connectWallet} disabled={loading}>
          {account ? "Reconnect Wallet" : "Connect MetaMask"}
        </button>

        {account && (
          <>
            <p><strong>Connected account:</strong></p>
            <p className="address">{account}</p>
          </>
        )}
      </section>

      {contractData && (
        <>
          <section className="panel">
            <h2>Escrow Information</h2>

            <p><strong>Contract:</strong></p>
            <p className="address">{CONTRACT_ADDRESS}</p>

            <p><strong>Client:</strong> {contractData.client}</p>
            <p><strong>Freelancer:</strong> {contractData.freelancer}</p>
            <p><strong>Arbitrator:</strong> {contractData.arbitrator}</p>
            <p><strong>Amount:</strong> {contractData.amount} ETH</p>

            <h3>Current Status: {contractData.status}</h3>

            <button
              onClick={() => loadContractData()}
              disabled={loading}
            >
              Refresh Contract
            </button>
          </section>

          <section className="panel">
            <h2>Escrow Progress</h2>

            <p>Funded: {String(contractData.funded)}</p>
            <p>Work submitted: {String(contractData.submitted)}</p>
            <p>Approved: {String(contractData.approved)}</p>
            <p>Released: {String(contractData.released)}</p>
            <p>Refunded: {String(contractData.refunded)}</p>
            <p>Disputed: {String(contractData.disputed)}</p>
          </section>

          {isClient && (
            <section className="panel">
              <h2>Client Dashboard</h2>

              {!contractData.funded && (
                <button disabled={loading} onClick={deposit}>
                  Fund Escrow ({contractData.amount} ETH)
                </button>
              )}

              {contractData.submitted &&
                !contractData.approved &&
                !contractData.disputed && (
                  <button disabled={loading} onClick={approveWork}>
                    Approve Work
                  </button>
              )}

              {contractData.approved &&
                !contractData.released &&
                !contractData.disputed && (
                  <button disabled={loading} onClick={releasePayment}>
                    Release Payment
                  </button>
              )}

              {contractData.funded &&
                !contractData.submitted &&
                !contractData.refunded && (
                  <button disabled={loading} onClick={refundPayment}>
                    Refund Client
                  </button>
              )}

              {contractData.funded &&
                !contractData.released &&
                !contractData.refunded &&
                !contractData.disputed && (
                  <button disabled={loading} onClick={raiseDispute}>
                    Raise Dispute
                  </button>
              )}
            </section>
          )}

          {isFreelancer && (
            <section className="panel">
              <h2>Freelancer Dashboard</h2>

              {contractData.funded &&
                !contractData.submitted &&
                !contractData.disputed &&
                !contractData.released && (
                  <button disabled={loading} onClick={submitWork}>
                    Submit Work
                  </button>
              )}

              {contractData.funded &&
                !contractData.released &&
                !contractData.refunded &&
                !contractData.disputed && (
                  <button disabled={loading} onClick={raiseDispute}>
                    Raise Dispute
                  </button>
              )}
            </section>
          )}

          {isArbitrator && contractData.disputed && (
            <section className="panel">
              <h2>Arbitrator Dashboard</h2>

              <p>Choose the outcome of this dispute:</p>

              <button
                disabled={loading}
                onClick={() => resolveDispute(true)}
              >
                Release Payment to Freelancer
              </button>

              <button
                disabled={loading}
                onClick={() => resolveDispute(false)}
              >
                Refund Client
              </button>
            </section>
          )}

          {!isClient && !isFreelancer && !isArbitrator && (
            <section className="panel">
              <p>
                This wallet is not registered as a participant
                in this escrow.
              </p>
            </section>
          )}
        </>
      )}

      {message && (
        <section className="panel message">
          <p>{message}</p>
        </section>
      )}

      {loading && <p className="loading">Transaction processing...</p>}
    </main>
  );
}

export default App;
