/**
 * Fasal Drishti AI - Landing Page Interactive Logic
 * Handles QR Generation, Mobile Nav, Modals, Smooth Scroll, and FAQ Accordions.
 */

document.addEventListener('DOMContentLoaded', () => {
  const DOWNLOAD_URL = 'https://neo-files-transfer.pages.dev/download/mtre516vdmlr4152f2ab';

  // 1. Generate Dynamic QR Code using QRCode CDN
  function generateQRCodes() {
    const heroQrContainer = document.getElementById('heroQrCode');
    const modalQrContainer = document.getElementById('modalQrCode');

    const qrConfig = {
      text: DOWNLOAD_URL,
      width: 160,
      height: 160,
      colorDark: '#064E3B',
      colorLight: '#FFFFFF',
      correctLevel: QRCode.CorrectLevel.H
    };

    if (heroQrContainer && typeof QRCode !== 'undefined') {
      heroQrContainer.innerHTML = '';
      new QRCode(heroQrContainer, { ...qrConfig, width: 150, height: 150 });
    }

    if (modalQrContainer && typeof QRCode !== 'undefined') {
      modalQrContainer.innerHTML = '';
      new QRCode(modalQrContainer, { ...qrConfig, width: 180, height: 180 });
    }
  }

  // Attempt QR code generation
  if (typeof QRCode !== 'undefined') {
    generateQRCodes();
  } else {
    window.addEventListener('load', generateQRCodes);
  }

  // 2. Mobile Navigation Drawer Control
  const menuBtn = document.getElementById('menuBtn');
  const closeMenuBtn = document.getElementById('closeMenuBtn');
  const mobileNavDrawer = document.getElementById('mobileNavDrawer');
  const drawerBackdrop = document.getElementById('drawerBackdrop');
  const mobileNavLinks = document.querySelectorAll('.mobile-nav-link');

  function openMobileMenu() {
    mobileNavDrawer.classList.add('open');
    drawerBackdrop.classList.add('active');
    document.body.style.overflow = 'hidden';
  }

  function closeMobileMenu() {
    mobileNavDrawer.classList.remove('open');
    drawerBackdrop.classList.remove('active');
    document.body.style.overflow = '';
  }

  if (menuBtn) menuBtn.addEventListener('click', openMobileMenu);
  if (closeMenuBtn) closeMenuBtn.addEventListener('click', closeMobileMenu);
  if (drawerBackdrop) drawerBackdrop.addEventListener('click', closeMobileMenu);

  // Auto-close menu when any mobile link is clicked
  mobileNavLinks.forEach(link => {
    link.addEventListener('click', () => {
      closeMobileMenu();
    });
  });

  // Smooth Scroll & Clean URL (Prevents /#features or hash anchors in address bar)
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
      const targetId = this.getAttribute('href');
      if (targetId && targetId !== '#') {
        const targetElement = document.querySelector(targetId);
        if (targetElement) {
          e.preventDefault();
          const headerOffset = 80;
          const elementPosition = targetElement.getBoundingClientRect().top;
          const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

          window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth'
          });

          // Ensure browser URL stays clean (e.g. fasaldrishti-ai.vercel.app without #)
          if (window.history && window.history.replaceState) {
            window.history.replaceState(null, document.title, window.location.pathname + window.location.search);
          }
        }
      } else if (targetId === '#') {
        e.preventDefault();
        window.scrollTo({ top: 0, behavior: 'smooth' });
        if (window.history && window.history.replaceState) {
          window.history.replaceState(null, document.title, window.location.pathname + window.location.search);
        }
      }
    });
  });

  // Clean any existing hash on initial page load
  if (window.location.hash && window.history && window.history.replaceState) {
    setTimeout(() => {
      window.history.replaceState(null, document.title, window.location.pathname + window.location.search);
    }, 150);
  }

  // 3. Modals System (Download, Terms, Privacy)
  const downloadModal = document.getElementById('downloadModal');
  const termsModal = document.getElementById('termsModal');
  const privacyModal = document.getElementById('privacyModal');

  function openModal(modal) {
    if (!modal) return;
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    closeMobileMenu();
  }

  function closeModal(modal) {
    if (!modal) return;
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }

  // Download Modal Triggers
  document.querySelectorAll('.trigger-download-modal').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openModal(downloadModal);
    });
  });

  // Terms Modal Triggers
  document.querySelectorAll('.trigger-terms-modal').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openModal(termsModal);
    });
  });

  // Privacy Modal Triggers
  document.querySelectorAll('.trigger-privacy-modal').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openModal(privacyModal);
    });
  });

  // Modal Close Buttons & Backdrop click
  document.querySelectorAll('.modal-close-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      closeModal(downloadModal);
      closeModal(termsModal);
      closeModal(privacyModal);
    });
  });

  [downloadModal, termsModal, privacyModal].forEach(modal => {
    if (modal) {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          closeModal(modal);
        }
      });
    }
  });

  // Close on Escape Key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeModal(downloadModal);
      closeModal(termsModal);
      closeModal(privacyModal);
      closeMobileMenu();
    }
  });

  // 4. Copy Download Link with Feedback Tooltip
  const copyLinkBtns = document.querySelectorAll('.btn-copy-link');
  copyLinkBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      navigator.clipboard.writeText(DOWNLOAD_URL).then(() => {
        const originalText = btn.innerHTML;
        btn.innerHTML = `<span>✅ Copied Link!</span>`;
        setTimeout(() => {
          btn.innerHTML = originalText;
        }, 2200);
      });
    });
  });

  // 5. FAQ Accordion Interaction
  const faqItems = document.querySelectorAll('.faq-item');
  faqItems.forEach(item => {
    const questionBtn = item.querySelector('.faq-question');
    if (questionBtn) {
      questionBtn.addEventListener('click', () => {
        const isActive = item.classList.contains('active');
        // Close other open faqs
        faqItems.forEach(otherItem => otherItem.classList.remove('active'));
        if (!isActive) {
          item.classList.add('active');
        }
      });
    }
  });

  // 6. Stats Counter Animation
  let animatedStats = false;
  const statsSection = document.querySelector('.stats-strip-section');
  if (statsSection) {
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !animatedStats) {
        animatedStats = true;
        document.querySelectorAll('.stat-number').forEach(counter => {
          const target = parseInt(counter.getAttribute('data-target') || '0', 10);
          if (target > 0) {
            let current = 0;
            const increment = Math.ceil(target / 40);
            const timer = setInterval(() => {
              current += increment;
              if (current >= target) {
                counter.innerText = target + (counter.getAttribute('data-suffix') || '');
                clearInterval(timer);
              } else {
                counter.innerText = current + (counter.getAttribute('data-suffix') || '');
              }
            }, 30);
          }
        });
      }
    }, { threshold: 0.3 });
    observer.observe(statsSection);
  }
});
